module jmyspell.core {
    requires java.base;
    exports org.dts.spell;
    exports org.dts.spell.dictionary;
    exports org.dts.spell.dictionary.myspell;
    exports org.dts.spell.dictionary.myspell.wordmaps;
    exports org.dts.spell.event;
    exports org.dts.spell.finder;
    exports org.dts.spell.filter;
    exports org.dts.spell.tokenizer;
}