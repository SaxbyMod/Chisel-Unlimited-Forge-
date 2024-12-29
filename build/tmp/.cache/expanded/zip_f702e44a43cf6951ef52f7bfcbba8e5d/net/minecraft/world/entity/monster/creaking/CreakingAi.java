package net.minecraft.world.entity.monster.creaking;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class CreakingAi {
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Creaking>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS
    );
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.NEAREST_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.PATH,
        MemoryModuleType.ATTACK_TARGET,
        MemoryModuleType.ATTACK_COOLING_DOWN
    );

    static void initCoreActivity(Brain<Creaking> pBrain) {
        pBrain.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim<Creaking>(0.8F) {
            protected boolean checkExtraStartConditions(ServerLevel p_365067_, Creaking p_367896_) {
                return p_367896_.canMove() && super.checkExtraStartConditions(p_365067_, p_367896_);
            }
        }, new LookAtTargetSink(45, 90), new MoveToTargetSink()));
    }

    static void initIdleActivity(Brain<Creaking> pBrain) {
        pBrain.addActivity(
            Activity.IDLE,
            10,
            ImmutableList.of(
                StartAttacking.create(
                    (p_369678_, p_369677_) -> p_369677_.isActive(), (p_369697_, p_366095_) -> p_366095_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
                ),
                SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)),
                new RunOne<>(
                    ImmutableList.of(
                        Pair.of(RandomStroll.stroll(0.3F), 2), Pair.of(SetWalkTargetFromLookTarget.create(0.3F, 3), 2), Pair.of(new DoNothing(30, 60), 1)
                    )
                )
            )
        );
    }

    static void initFightActivity(Brain<Creaking> pBrain) {
        pBrain.addActivityAndRemoveMemoryWhenStopped(
            Activity.FIGHT,
            10,
            ImmutableList.of(
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                MeleeAttack.create(Creaking::canMove, 40),
                StopAttackingIfTargetInvalid.create()
            ),
            MemoryModuleType.ATTACK_TARGET
        );
    }

    public static Brain.Provider<Creaking> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    public static Brain<Creaking> makeBrain(Brain<Creaking> pBrain) {
        initCoreActivity(pBrain);
        initIdleActivity(pBrain);
        initFightActivity(pBrain);
        pBrain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        pBrain.setDefaultActivity(Activity.IDLE);
        pBrain.useDefaultActivity();
        return pBrain;
    }

    public static void updateActivity(Creaking pCreaking) {
        if (!pCreaking.canMove()) {
            pCreaking.getBrain().useDefaultActivity();
        } else {
            pCreaking.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
        }
    }
}