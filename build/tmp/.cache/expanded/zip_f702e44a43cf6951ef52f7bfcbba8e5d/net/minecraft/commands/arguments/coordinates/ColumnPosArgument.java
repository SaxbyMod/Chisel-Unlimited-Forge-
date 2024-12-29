package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ColumnPos;

public class ColumnPosArgument implements ArgumentType<Coordinates> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "~1 ~-2", "^ ^", "^-1 ^0");
    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.translatable("argument.pos2d.incomplete"));

    public static ColumnPosArgument columnPos() {
        return new ColumnPosArgument();
    }

    public static ColumnPos getColumnPos(CommandContext<CommandSourceStack> pContext, String pName) {
        BlockPos blockpos = pContext.getArgument(pName, Coordinates.class).getBlockPos(pContext.getSource());
        return new ColumnPos(blockpos.getX(), blockpos.getZ());
    }

    public Coordinates parse(StringReader pReader) throws CommandSyntaxException {
        int i = pReader.getCursor();
        if (!pReader.canRead()) {
            throw ERROR_NOT_COMPLETE.createWithContext(pReader);
        } else {
            WorldCoordinate worldcoordinate = WorldCoordinate.parseInt(pReader);
            if (pReader.canRead() && pReader.peek() == ' ') {
                pReader.skip();
                WorldCoordinate worldcoordinate1 = WorldCoordinate.parseInt(pReader);
                return new WorldCoordinates(worldcoordinate, new WorldCoordinate(true, 0.0), worldcoordinate1);
            } else {
                pReader.setCursor(i);
                throw ERROR_NOT_COMPLETE.createWithContext(pReader);
            }
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
        if (!(pContext.getSource() instanceof SharedSuggestionProvider)) {
            return Suggestions.empty();
        } else {
            String s = pBuilder.getRemaining();
            Collection<SharedSuggestionProvider.TextCoordinates> collection;
            if (!s.isEmpty() && s.charAt(0) == '^') {
                collection = Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL);
            } else {
                collection = ((SharedSuggestionProvider)pContext.getSource()).getRelevantCoordinates();
            }

            return SharedSuggestionProvider.suggest2DCoordinates(s, collection, pBuilder, Commands.createValidator(this::parse));
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}