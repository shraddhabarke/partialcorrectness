package ast

import org.apache.commons.lang3.StringUtils

trait TernaryOpNode[T] extends ASTNode{
  val arg0: ASTNode
  val arg1: ASTNode
  val arg2: ASTNode
  override val height: Int = 1 + Math.max(arg0.height,Math.max(arg1.height,arg2.height))
  override val terms: Int = 1 + arg0.terms + arg1.terms + arg2.terms
  assert(arg0.values.length == arg1.values.length && arg1.values.length == arg2.values.length)
  def doOp(a0: Any, a1: Any, a2: Any): T
  lazy val values : List[T] =
    arg0.values.zip(arg1.values).zip(arg2.values).map(tup => doOp(tup._1._1, tup._1._2, tup._2)).toList

  override val children: Iterable[ASTNode] = Iterable(arg0,arg1,arg2)

  def includes(varName: String): Boolean = arg0.includes(varName) || arg1.includes(varName) || arg2.includes(varName)
}

class StringReplace(val arg0: StringNode, val arg1: StringNode, val arg2: StringNode, var prior: Int = 0) extends TernaryOpNode[String] with StringNode {
  override def doOp(a0: Any, a1: Any, a2: Any): String =
    StringUtils.replaceOnce(a0.asInstanceOf[String],a1.asInstanceOf[String],a2.asInstanceOf[String])

  override lazy val code: String = List(arg0.code,arg1.code,arg2.code).mkString("(str.replace "," ",")")
  override def cost: Int = prior + children.toList.map(c => c.cost).sum
}

class StringITE(val arg0: BoolNode, val arg1: StringNode, val arg2: StringNode, var prior: Int = 0) extends TernaryOpNode[String] with StringNode {
  override def doOp(a0: Any, a1: Any, a2: Any): String = if (a0.asInstanceOf[Boolean]) a1.asInstanceOf[String] else a2.asInstanceOf[String]

  override lazy val code: String = List(arg0.code,arg1.code,arg2.code).mkString("(ite "," ",")")
  override def cost: Int = prior + children.toList.map(c => c.cost).sum
}

class IntITE(val arg0: BoolNode, val arg1: IntNode, val arg2: IntNode, var prior: Int = 0) extends TernaryOpNode[Int] with IntNode {
  override def doOp(a0: Any, a1: Any, a2: Any): Int = if (a0.asInstanceOf[Boolean]) a1.asInstanceOf[Int] else a2.asInstanceOf[Int]

  override lazy val code: String = List(arg0.code,arg1.code,arg2.code).mkString("(ite "," ",")")
  override def cost: Int = prior + children.toList.map(c => c.cost).sum
}

class Substring(val arg0: StringNode, val arg1: IntNode, val arg2: IntNode, var prior: Int = 0) extends TernaryOpNode[String] with StringNode {
  override def doOp(a0: Any, a1: Any, a2: Any): String = {
    //replacing eusolver def with the cvc4/z3 def:
    //if (i.strictlyNegative() || j.strictlyNegative() || i >= s_len)
    //  results[currNode] = EvalResult(String(""));
    //else if (i + j > s_len)
    //  EvalResult(s.suffix((s_len - i).toUnsignedInt()));
    //else EvalResult(s.substr(i.toUnsignedInt(), j.toUnsignedInt()));
    val a = a0.asInstanceOf[String]
    val b = a1.asInstanceOf[Int]
    val c = a2.asInstanceOf[Int]
    if (b < 0 || c < 0 || b >= a.length) ""
    else a.drop(b).take(c)
  }

  override lazy val code: String = List(arg0.code,arg1.code,arg2.code).mkString("(str.substr "," ",")")
  override def cost: Int = prior + children.toList.map(c => c.cost).sum}

class IndexOf(val arg0: StringNode, val arg1: StringNode, val arg2: IntNode, var prior: Int = 0) extends TernaryOpNode[Int] with IntNode {
  override def doOp(a0: Any, a1: Any, a2: Any): Int = a0.asInstanceOf[String].indexOf(a1.asInstanceOf[String],a2.asInstanceOf[Int])

  override lazy val code: String = List(arg0.code,arg1.code,arg2.code).mkString("(str.indexof "," ",")")
  override def cost: Int = prior + children.toList.map(c => c.cost).sum}