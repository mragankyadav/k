package org.kframework.definition

import org.apache.commons.lang3.StringEscapeUtils
import org.kframework.attributes.Att
import org.kframework.builtin.Sorts
import org.kframework.kore.ADT._
import org.kframework.kore._
import org.kframework.parser.concrete2kore.generator.RuleGrammarGenerator

import collection.JavaConverters._

/**
  * Created by lpena on 10/11/16.
  */

object KDefinitionDSL {
  def asKApply(label: String, values: List[String]): K =
    KORE.KApply(KORE.KLabel(label), KORE.KList(values map { value => KORE.KToken(value, Sorts.KString, Att()) }), Att())
  def asKApply(label: String, values: String*): K = asKApply(label, values toList)
  def apply(label: String, values: K*): K = KORE.KApply(KORE.KLabel(label), KORE.KList(values toList), Att())

  implicit def asAttribute(str: String): K = asKApply(str, List.empty)
  implicit def asNonTerminal(s: ADT.SortLookup): NonTerminal = NonTerminal(s)
  implicit def asTerminal(s: String): Terminal = Terminal(s)
  implicit def asProduction(ps: ProductionItem*): Seq[ProductionItem] = ps
  implicit def asSentence(bp: BecomingSyntax): Sentence = Production(bp.sort, bp.pis, Att())
  implicit def asSentence(bp: BecomingSyntaxSort): Sentence = SyntaxSort(bp.sort, Att())

  def Sort(s: String): ADT.SortLookup = ADT.SortLookup(s)

  def regex(s: String): ProductionItem = RegexTerminal("#", s, "#")

  case class syntax(s: ADT.SortLookup) {
    def is(pis: ProductionItem*): BecomingSyntax = BecomingSyntax(s, pis)
  }
  case class BecomingSyntax(sort: ADT.SortLookup, pis: Seq[ProductionItem]) {
    def att(atts: K*): Production = Production(sort, pis, atts.foldLeft(Att())(_+_))
  }

  def sort(sort: ADT.SortLookup): BecomingSyntaxSort = BecomingSyntaxSort(sort)
  case class BecomingSyntaxSort(sort: ADT.SortLookup) {
    def att(atts: K*): SyntaxSort = SyntaxSort(sort, atts.foldLeft(Att())(_+_))
  }

  def rule(rewrite: ADT.KRewrite): Rule = Rule(rewrite, KORE.KToken("true", ADT.SortLookup("Bool")), KORE.KToken("true", ADT.SortLookup("Bool")))
  def rewrite(left: K, right: K): ADT.KRewrite = ADT.KRewrite(left, right)

  def >(labels: String*): Set[Tag] = labels map Tag toSet
  def priority(labels: Set[Tag]*): SyntaxPriority = SyntaxPriority(labels)

  def imports(s: Module*): Set[Module] = s toSet
  def sentences(s: Sentence*): Set[Sentence] = s toSet

  def khook(label: String): K = asKApply("khook", List(label))
  def klabel(label: String): K = asKApply("klabel", List(label))
  def ktoken(label: String): K = asKApply("ktoken", List(label))
  def kunit(label: String): K = asKApply("unit", label)
}

object ExpDefinition {
  import KDefinitionDSL._

//  KDefinition(.KRequireList(),KModuleList(KModule(#token("EXP",KModuleName@KDEFINITION),.KImportList(),
  // KSentenceList(KSentenceWithAttributes(KProduction(#token("Exp",KSort@KSENTENCES),KProductionItems(#token("Exp",KSort@KSENTENCES),KProductionItems(#token("+",KString),#token("Exp",KSort@KSENTENCES)))),KAttributes(KAttributeApply(#token("klabel",KAttributeKey@KATTRIBUTES),#token("p",KAttributeKey@KATTRIBUTES)),#token("plus",KAttributeKey@KATTRIBUTES))),
  // KSentenceList(KSentenceWithAttributes(KProduction(#token("Exp",KSort@KSENTENCES),KProductionItems(#token("Exp",KSort@KSENTENCES),KProductionItems(#token("-",KString),#token("Exp",KSort@KSENTENCES)))),KAttributes(#token("minus",KAttributeKey@KATTRIBUTES),KAttributeApply(#token("klabel",KAttributeKey@KATTRIBUTES),#token("m",KAttributeKey@KATTRIBUTES)))),
  // KSentenceList(KSentenceWithAttributes(KProduction(#token("Exp",KSort@KSENTENCES),KProductionItems(#token("Exp",KSort@KSENTENCES),KProductionItems(#token("*",KString),#token("Exp",KSort@KSENTENCES)))),KAttributes(KAttributeApply(#token("klabel",KAttributeKey@KATTRIBUTES),#token("t",KAttributeKey@KATTRIBUTES)),#token("times",KAttributeKey@KATTRIBUTES))),
  // KSentenceList(KSentenceWithAttributes(KProduction(#token("Exp",KSort@KSENTENCES),KProductionItems(#token("Exp",KSort@KSENTENCES),KProductionItems(#token("/",KString),#token("Exp",KSort@KSENTENCES)))),KAttributes(KAttributeApply(#token("klabel",KAttributeKey@KATTRIBUTES),#token("d",KAttributeKey@KATTRIBUTES)),#token("div",KAttributeKey@KATTRIBUTES))),
  // KRule(#token("3 + 3 => 6\n      ",KBubble@KBUBBLE))))))),.KModuleList()))


  val expString =
    """
      module EXP
        syntax Exp ::= Exp "+" Exp [klabel(p), plus]
        syntax Exp ::= Exp "-" Exp [minus, klabel(m)]
        syntax Exp ::= Exp "*" Exp [klabel(t), times]
        syntax Exp ::= Exp "/" Exp [klabel(d), div]
        syntax Exp ::= "0"
        syntax Exp ::= "1"
        syntax Exp ::= "2"
        syntax Exp ::= "3"
        syntax Exp ::= "4"
        syntax Exp ::= "5"
        syntax Exp ::= "6"
        syntax Exp ::= "7"
        syntax Exp ::= "8"
        syntax Exp ::= "9"
        rule 3 + 3 => 6
        rule 9 - 4 => 5
        rule 7 * 0 => 0
      endmodule
    """

  val Bool = Sort("Bool")
  val BOOL = Module("BOOL", imports(), sentences(
    sort(Bool),
    syntax(Bool) is "true" att klabel("true"),
    syntax(Bool) is "false" att klabel("false")
  ))

  val Exp = Sort("Exp")
  val EXP = Module("EXP", imports(BOOL), sentences(
    syntax(Exp) is (Exp, "+", Exp) att(klabel("p"), "plus"),
    syntax(Exp) is (Exp, "-", Exp) att("minus", klabel("m")),
    syntax(Exp) is (Exp, "*", Exp) att(klabel("t"), "times"),
    syntax(Exp) is (Exp, "/", Exp) att(klabel("d"), "div"),
    priority( >("p", "t") , >("m", "d") ),
    rule(rewrite(KApply(KLabelLookup("p"), KList(List(KToken("3", Exp), KToken("3", Exp)))), KToken("6", Exp)))
  ))
}

object KoreDefintion {
  import KDefinitionDSL._


  // ### KSTRING
  val KRegexString = "[\"](([^\n\r\t\f\"\\\\])|([\\\\][nrtf\"\\\\])|([\\\\][x][0-9a-fA-F]{2})|([\\\\][u][0-9a-fA-F]{4})|([\\\\][U][0-9a-fA-F]{8}))*[\"]"

  val KString = Sort("KString")

  val KSTRING = Module("KSTRING", imports(), sentences(
    syntax(KString) is regex(KRegexString) att("token", khook("org.kframework.kore.KString"))
  ))


  // ### KBUBBLE
  val KBubbleRegex = "[^ \t\n\r]+"

  val KBubbleItem = Sort("KBubbleItem")
  val KBubble = Sort("KBubble")

  val KBUBBLE = Module("KBUBBLE", imports(), sentences(
    // TODO: Must make the parser actually accept reject2 in this format (as opposed to vertical bars)
    syntax(KBubbleItem) is regex(KBubbleRegex) att("token", asKApply("reject2", "rule|syntax|endmodule|configuration|context")),
    syntax(KBubble) is (KBubble, KBubbleItem) att "token",
    syntax(KBubble) is KBubbleItem att "token"
  ))


  // ### KATTRIBUTES
  val KRegexAttributeKey1 = "[\\.A-Za-z\\-0-9]*"
  val KRegexAttributeKey2 = "`(\\\\`|\\\\\\\\|[^`\\\\\n\r\t\f])+`"
  val KRegexAttributeKey3 = "(?![a-zA-Z0-9])[#a-z][a-zA-Z0-9@\\-]*"
  // TODO: the (?<! is a signal to the parser that it should be used as a "precedes" clause, do we need it?
  // val KRegexAttributeKey3 = """(?<![a-zA-Z0-9])[#a-z][a-zA-Z0-9@\\-]*"""

  val KAttributeKey = Sort("KAttributeKey")
  val KKeyList = Sort("KKeyList")
  val KKeySet = Sort("KKeySet")
  val KAttribute= Sort("KAttribute")
  val KAttributes= Sort("KAttributes")

  val KATTRIBUTES = Module("KATTRIBUTES", imports(), sentences(
    syntax(KAttributeKey) is regex(KRegexAttributeKey1) att("token", khook("org.kframework.kore.KLabel")),
    syntax(KAttributeKey) is regex(KRegexAttributeKey2) att("token", khook("org.kframework.kore.KLabel")),
    syntax(KAttributeKey) is regex(KRegexAttributeKey3) att("token", khook("org.kframework.kore.KLabel"), "autoReject"),

    syntax(KKeyList) is KAttributeKey,
    syntax(KKeyList) is "" att klabel(".KKeyList"),
    syntax(KKeyList) is (KKeyList, ",", KKeyList) att(klabel("KKeyList"), "assoc", kunit(".KKeyList")),

    syntax(KKeySet) is KAttributeKey,
    syntax(KKeySet) is "" att klabel(".KKeySet"),
    syntax(KKeySet) is (KKeySet, KKeySet) att(klabel("KKeySet"), "assoc", "comm", kunit(".KKeyList")),

    syntax(KAttribute) is KAttributeKey,
    syntax(KAttribute) is (KAttributeKey, "(", KKeyList, ")") att klabel("KAttributeApply"),

    syntax(KAttributes) is KAttribute,
    syntax(KAttributes) is "" att klabel(".KAttributes"),
    syntax(KAttributes) is (KAttribute, ",", KAttributes) att klabel("KAttributes")
  ))


  // ### KML
  val KMLVar = Sort("KMLVar")
  val KMLFormula = Sort("KMLFormula")

  val KML = Module("KML", imports(KSTRING), sentences(
    sort(KMLVar) att klabel("KMLVar"),

    syntax(KMLFormula) is KMLVar,
    syntax(KMLFormula) is "tt" att klabel("KMLtrue"),
    syntax(KMLFormula) is "ff" att klabel("KMLfalse"),

    syntax(KMLFormula) is ("~", KMLFormula) att klabel("KMLnot"),
    syntax(KMLFormula) is (KMLFormula, "/\\", KMLFormula) att klabel("KMLand"),
    syntax(KMLFormula) is (KMLFormula, "\\/", KMLFormula) att klabel("KMLor"),

    syntax(KMLFormula) is ("E", KMLVar, ".", KMLFormula) att klabel("KMLexists"),
    syntax(KMLFormula) is ("A", KMLVar, ".", KMLFormula) att klabel("KMLforall"),

    syntax(KMLFormula) is (KMLFormula, "=>", KMLFormula) att klabel("KMLnext")
  ))


  // ### KSENTENCES
  val KRegexSort = "[A-Z][A-Za-z0-9]*"

  val KSort = Sort("KSort")

  val KTerminal = Sort("KTerminal")
  val KNonTerminal = Sort("KNonTerminal")

  val KProductionItems = Sort("KProductionItems")
  val KProduction = Sort("KProduction")
  val KProductions = Sort("KProductions")

  val KPriority = Sort("KPriority")

  val KSentence = Sort("KSentence")
  val KSentenceList = Sort("KSentenceList")

  val KSENTENCES = Module("KSENTENCES", imports(KSTRING, KBUBBLE, KATTRIBUTES), sentences(
    syntax(KSort) is regex(KRegexSort) att("token", klabel("KSort")),

    syntax(KTerminal) is KString,
    syntax(KTerminal) is ("r", KString) att klabel("KRegex"),
    syntax(KNonTerminal) is KSort,

    syntax(KProduction) is KTerminal,
    syntax(KProduction) is KNonTerminal,
    syntax(KProduction) is (KProduction, KProduction) att(klabel("KProductionItems"), "assoc"),

    syntax(KPriority) is KKeySet,
    syntax(KPriority) is (KPriority, ">", KPriority) att(klabel("KPriorityItems"), "assoc"),

    syntax(KSentence) is ("syntax", KSort) att klabel("KSortDecl"),
    syntax(KSentence) is ("syntax", KSort, "::=", KProduction) att klabel("KProduction"),
    syntax(KSentence) is ("syntax", "priority", KPriority) att klabel("KPriority"),
    syntax(KSentence) is ("rule", KBubble) att klabel("KRule"),
    syntax(KSentence) is (KSentence, "[", KAttributes, "]") att klabel("KSentenceWithAttributes"),

    syntax(KSentenceList) is KSentence,
    syntax(KSentenceList) is "" att klabel(".KSentenceList"),
    syntax(KSentenceList) is (KSentence, KSentenceList) att klabel("KSentenceList")
    // TODO: Why doesn't this work?
    //syntax(KSentenceList) is (KSentenceList, KSentenceList) att(klabel("KSentenceList"), "assoc", "comm", kunit(".KSentenceList"))
  ))


  // ### KDEFINITION
  val KRegexModuleName = "[A-Z][A-Z\\-]*"

  val KRequire = Sort("KRequire")
  val KRequireList = Sort("KRequireList")

  val KModuleName = Sort("KModuleName")
  val KImport = Sort("KImport")
  val KImportList = Sort("KImportList")

  val KModule = Sort("KModule")
  val KModuleList = Sort("KModuleList")

  val KDefinition = Sort("KDefinition")

  val KDEFINITION = Module("KDEFINITION", imports(KSENTENCES), sentences(
    syntax(KRequire) is ("require", KString) att klabel("KRequire"),
    syntax(KRequireList) is "" att klabel(".KRequireList"),
    syntax(KRequireList) is (KRequire, KRequireList) att klabel("KRequireList"),

    syntax(KModuleName) is regex(KRegexModuleName) att("token", klabel("KModuleName")),
    syntax(KImport) is ("imports", KModuleName) att klabel("KImport"),
    syntax(KImportList) is "" att klabel(".KImportList"),
    syntax(KImportList) is (KImport, KImportList) att klabel("KImportList"),

    syntax(KModule) is ("module", KModuleName, KImportList, KSentenceList, "endmodule") att klabel("KModule"),
    syntax(KModuleList) is "" att klabel(".KModuleList"),
    syntax(KModuleList) is (KModule, KModuleList) att klabel("KModuleList"),

    syntax(KDefinition) is (KRequireList, KModuleList) att klabel("KDefinition")
  ))


  // ### KORE
  val KORE = Map( "KSTRING" -> KSTRING
                , "KBUBBLE" -> KBUBBLE
                , "KATTRIBUTES" -> KATTRIBUTES
                , "KML" -> KML
                , "KSENTENCES" -> KSENTENCES
                , "KDEFINITION" -> KDEFINITION
                )
}

object KoreSyntaxDown {
  import KDefinitionDSL._
  import KoreDefintion._
  import ADT.KList

  def downKKeyList(parsedKKeyList: K): List[String] = parsedKKeyList match {
    case KApply(KLabelLookup("KKeyList"), KList(kkeys), _) => kkeys flatMap downKKeyList
    case KToken(att, KAttributeKey, _)                     => List(att)
    case _                                                 => List.empty
  }

  def downKKeySet(parsedKKeySet: K): Set[String] = parsedKKeySet match {
    case KApply(KLabelLookup("KKeySet"), KList(kkeys), _) => kkeys.toSet flatMap downKKeySet
    case KToken(att, KAttributeKey, _)                    => Set(att)
    case _                                                => Set.empty
  }

  def downAttributes(parsedAttributes: K): Att = parsedAttributes match {
    case KApply(KLabelLookup("KAttributes"), KList(atts), _)                                              => atts.foldLeft(Att()) ((accAtt: Att, newAtt: K) => accAtt ++ downAttributes(newAtt))
    case KApply(KLabelLookup("KAttributeApply"), KList(KToken(fnc, KAttributeKey, _) :: keyList :: _), _) => Att(asKApply(fnc, downKKeyList(keyList)))
    case KToken(attName, KAttributeKey, _)                                                                => Att(attName)
    case _                                                                                                => Att()
  }

  def downProduction(parsedProduction: K): Seq[ProductionItem] = parsedProduction match {
    case KApply(KLabelLookup("KProductionItems"), KList(productionItems), _)    => productionItems flatMap downProduction
    case KApply(KLabelLookup("KRegex"), KList(KToken(str, KString, _) :: _), _) => Seq(RegexTerminal("#", str, "#"))
    case KToken(sortName, KSort, _)                                             => Seq(NonTerminal(Sort(sortName)))
    case KToken(str, KString, _)                                                => Seq(Terminal(str))
    case _                                                                      => Seq.empty
  }

  def downPriorityBlocks(parsedPriority: K): Seq[Set[Tag]] = parsedPriority match {
    case KApply(KLabelLookup("KPriorityItems"), KList(priorityBlocks), _) => priorityBlocks flatMap downPriorityBlocks
    case _                                                                => Seq(downKKeySet(parsedPriority) map Tag)
  }

  def downSentences(parsedSentence: K, atts: Att = Att()): Set[Sentence] = parsedSentence match {
    case KApply(KLabelLookup("KSentenceList"), KList(sentences), _)                                   => sentences.toSet flatMap ((pS: K) => downSentences(pS, Att()))
    case KApply(KLabelLookup("KSentenceWithAttributes"), KList(sentence :: newAtts :: _), _)          => downSentences(sentence, downAttributes(newAtts) ++ atts)
    case KApply(KLabelLookup("KSortDecl"), KList(KToken(sortName, KSort, _) :: _), _)                 => Set(SyntaxSort(Sort(sortName), atts))
    case KApply(KLabelLookup("KProduction"), KList(KToken(sortName, KSort, _) :: production :: _), _) => Set(Production(Sort(sortName), downProduction(production), atts))
    case KApply(KLabelLookup("KPriority"), KList(priority :: _), _)                                   => Set(SyntaxPriority(downPriorityBlocks(priority), atts))
    case _                                                                                            => Set.empty
  }

  def downImports(parsedImports: K): List[String] = parsedImports match {
    case KApply(KLabelLookup("KImportList"), KList(importStmt :: rest :: _), _)               => downImports(importStmt) ++ downImports(rest)
    case KApply(KLabelLookup("KImport"), KList(KToken(importModule, KModuleName, _) :: _), _) => List(importModule)
    case _                                                                                    => List.empty
  }

  // TODO: Make this chase the requires list
  def downModules(parsedModule: K, downedModules: Map[String, Module]): Map[String, Module] = parsedModule match {
    case KApply(KLabelLookup("KDefinition"), KList(requires :: modules :: _), _)                              => downModules(modules, downModules(requires, downedModules))
    case KApply(KLabelLookup("KRequireList"), _, _)                                                           => downedModules
    case KApply(KLabelLookup("KModuleList"), KList(module :: modules :: _), _)                                => downModules(modules, downModules(module, downedModules))
    case KApply(KLabelLookup("KModule"), KList(KToken(name, KModuleName, _) :: imports :: sentences :: _), _) => downedModules ++ Map(name -> Module(name, downImports(imports) map downedModules toSet, downSentences(sentences)))
    case _                                                                                                    => downedModules
  }

  def preProcess(parsed: K): K = parsed match {
    case KToken(str, KString, atts)          => KToken(StringEscapeUtils.unescapeJava(str.drop(1).dropRight(1)), KString, atts)
    case kt@KToken(_, _, _)                  => kt
    case KApply(head, KList(subNodes), atts) => KApply(head, KList(subNodes map preProcess), atts)
  }

  def resolveRules(module: Module): Module = {

  }
//    val resolvedImports = (module.imports map resolveRules) + KML
//    val sorts = module.localSorts
//    val ruleSyntax = (module.localSorts map ((ls: org.kframework.kore.Sort) => Production(ls, KMLFormula, Att()))) ++ module.localSyntaxSentences
//    val rulesToParse = module.localSemanticSentences
//    Module(module.name, resolvedImports, module.localSentences ++ kmlSubsorts)
//    // separate module from rules
//    // import KML into module
//    // subsort everything in module to KMLFormula
//    // parse rules in resulting module as sort KMLFormula
//    // put rules back into original module
//  }


//    private Module resolveNonConfigBubbles(Module module, Function<String, Module> getProcessedModule, boolean isStrict) {
//        if (stream(module.localSentences())
//                .filter(s -> s instanceof Bubble)
//                .map(b -> (Bubble) b)
//                .filter(b -> !b.sentenceType().equals("config")).count() == 0)
//            return module;
//        Module ruleParserModule = RuleGrammarGenerator.getRuleGrammar(module, getProcessedModule);
//
//        ParseCache cache = loadCache(ruleParserModule);
//        ParseInModule parser = RuleGrammarGenerator.getCombinedGrammar(cache.getModule(), isStrict);
//
//        java.util.Set<Bubble> bubbles = stream(module.localSentences())
//                .parallel()
//                .filter(s -> s instanceof Bubble)
//                .map(b -> (Bubble) b).collect(Collectors.toSet());
//
//        Set<Sentence> ruleSet = bubbles.stream()
//                .filter(b -> b.sentenceType().equals("rule"))
//                .map(b -> performParse(cache.getCache(), parser, b))
//                .flatMap(r -> {
//                    if (r.isRight()) {
//                        return Stream.of(this.upRule(r.right().get()));
//                    } else {
//                        errors.addAll(r.left().get());
//                        return Stream.empty();
//                    }
//                }).collect(Collections.toSet());
//
//        Set<Sentence> contextSet = bubbles.stream()
//                .filter(b -> b.sentenceType().equals("context"))
//                .map(b -> performParse(cache.getCache(), parser, b))
//                .flatMap(r -> {
//                    if (r.isRight()) {
//                        return Stream.of(this.upContext(r.right().get()));
//                    } else {
//                        errors.addAll(r.left().get());
//                        return Stream.empty();
//                    }
//                }).collect(Collections.toSet());
//
//        return Module(module.name(), module.imports(),
//                stream((Set<Sentence>) module.localSentences().$bar(ruleSet).$bar(contextSet)).filter(b -> !(b instanceof Bubble)).collect(Collections.toSet()), module.att());
//    }

}
