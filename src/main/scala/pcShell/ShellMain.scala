package pcShell

import java.io.PrintWriter
import jline.console.ConsoleReader
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime._
import pcShell.Tabulator.GreenString
import sygus._


object ShellMain extends App {
  val taskFilename = args(0)
  val task = new SygusFileTask(scala.io.Source.fromFile(taskFilename).mkString)

  val errorColor = Console.RED
  val infoColor = Console.YELLOW

  def escapeWSAndQuote(s: String) = { //		if ( s==null ) return s;
    "\"" + s.replace("\n", "\\n")
     .replace("\r", "\\r")
     .replace("\t", "\\t") + "\""
  }

  def getTokenErrorDisplay(t: Token): String = {
    if (t == null) return "<no token>"
    var s = t.getText
    if (s == null) if (t.getType == Token.EOF) s = "<EOF>"
    else s = "<" + t.getType + ">"
    escapeWSAndQuote(s)
  }
  class EOFExpectedException(recognizer: Parser) extends RecognitionException(recognizer,recognizer.getInputStream(), recognizer.getContext) {
    this.setOffendingToken(recognizer.getCurrentToken)
  }

  def prettyPrintSyntaxError(exception: RecognitionException) = {
    val offset = if (exception.isInstanceOf[LexerNoViableAltException]) exception.asInstanceOf[LexerNoViableAltException].getStartIndex + 2 else exception.getOffendingToken.getStartIndex + 1
    out.print(errorColor)
    out.print(" " * (offset) + "^")
    if (exception.getOffendingToken != null && exception.getOffendingToken.getStopIndex - exception.getOffendingToken.getStartIndex > 1)
      out.print("-" * (exception.getOffendingToken.getStopIndex - exception.getOffendingToken.getStartIndex) + "^")
    out.println
    exception match {
      case e: NoViableAltException =>
        out.println("no viable alternative at input " + (if (e.getStartToken == Token.EOF) "<EOF>" else escapeWSAndQuote(e.getOffendingToken.getText)))
      case e: InputMismatchException =>
        out.println( s"mismatched input ${getTokenErrorDisplay(e.getOffendingToken())}, expecting ${e.getExpectedTokens().toString(e.getRecognizer.getVocabulary)}")
      case e: EOFExpectedException =>
        out.println( s"mismatched input ${getTokenErrorDisplay(e.getOffendingToken())}, expecting <EOF>")
      case e: LexerNoViableAltException =>
        out.println("bad token")
    }
    out.print(Console.RESET)
  }

  def escapeIfString(elem: Any): String = if (elem.isInstanceOf[String]) escapeWSAndQuote(elem.asInstanceOf[String]) else elem.toString

//  import jline.TerminalFactory
//  jline.TerminalFactory.registerFlavor(TerminalFactory.Flavor.WINDOWS, classOf[UnsupportedTerminal])
  val reader = new ConsoleReader()
  reader.setPrompt("> ")
  reader.setHistoryEnabled(true)

  val out = new PrintWriter(reader.getOutput)

  var line: String = null
  while ((line = reader.readLine()) != null) {
      if (!line.trim.isEmpty) try {
      if (line.trim.startsWith(":")) line.trim.drop(1) match {
        case "quit" | "q" => sys.exit(0)
        case "synt" | "s" => {
          out.println(s"${infoColor}Synthesizing... (Press any key to interrupt)${Console.RESET}")
          out.flush()
          val results = Main.synthesizeFromTask(task, 40, true).take(5)
          if (results.isEmpty) {
            out.println(s"${infoColor}No results, try waiting a bit longer${Console.RESET}")
          } else {
            val resultStrings = results.map(_.program.code)
            val expectedResults = task.examples.map(_.output)
            val fits = results.map(_.program.values.zip(expectedResults).count(pair => pair._1 == pair._2))
            val n = expectedResults.length
            resultStrings.reverse.foreach(reader.getHistory.add(_))
            resultStrings.zip(fits).zipWithIndex.foreach(
              { case ((p, fit), i) => out.println(s"$infoColor${i + 1}:${Console.RESET} $p $infoColor[$fit/$n]${Console.RESET}") })
          }
        }
        case _ => out.println("Not a valid command, try :quit or :synt")
      } else {
      val lexer = new SyGuSLexer(CharStreams.fromString(line))
      lexer.removeErrorListeners()
      lexer.addErrorListener(new ThrowingLexerErrorListener)
      val parser = new SyGuSParser(new BufferedTokenStream(lexer))
      parser.removeErrorListeners()
      parser.setErrorHandler(new BailErrorStrategy)
      val parsed = parser.bfTerm()
      if (parser.getCurrentToken.getType != Token.EOF)
        throw new EOFExpectedException(parser)
      val visitor = new ASTGenerator(task)
      val ast = visitor.visit(parsed)
      out.println(Tabulator.format(List("input","result","expected") +:
        task.examples.zip(ast.values).map(pair => List(pair._1.input.toList.map(kv =>
          s"${kv._1} -> ${escapeIfString(kv._2.toString)}"
        ).mkString("\n"),
          if (pair._2 == pair._1.output) GreenString(escapeIfString(pair._2)) else escapeIfString(pair._2),
          escapeIfString(pair._1.output)))))
    }} catch {
      case e: RecognitionException => {
        prettyPrintSyntaxError(e)
      }
      case e: ResolutionException => {
        val startIdx = e.badCtx.getStart.getStartIndex
        val len = e.badCtx.getStop.getStopIndex - startIdx + 1
        out.println(errorColor + " " * (startIdx + 2) + "^" + (if (len > 1) "-" * (len - 2) + "^" else ""))
        out.println("Cannot resolve program" + Console.RESET)
      }
      case e: ParseCancellationException =>{
        prettyPrintSyntaxError(e.getCause.asInstanceOf[RecognitionException])
      }
    }
  }

}
