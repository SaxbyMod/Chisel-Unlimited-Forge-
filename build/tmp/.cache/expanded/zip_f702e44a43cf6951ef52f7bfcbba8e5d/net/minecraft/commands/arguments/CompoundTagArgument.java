package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

public class CompoundTagArgument implements ArgumentType<CompoundTag> {
    private static final Collection<String> EXAMPLES = Arrays.asList("{}", "{foo=bar}");

    private CompoundTagArgument() {
    }

    public static CompoundTagArgument compoundTag() {
        return new CompoundTagArgument();
    }

    public static <S> CompoundTag getCompoundTag(CommandContext<S> pContext, String pName) {
        return pContext.getArgument(pName, CompoundTag.class);
    }

    public CompoundTag parse(StringReader pReader) throws CommandSyntaxException {
        return new TagParser(pReader).readStruct();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}