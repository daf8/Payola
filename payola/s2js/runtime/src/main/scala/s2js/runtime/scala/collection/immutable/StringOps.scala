package s2js.runtime.scala.collection.immutable

import s2js.compiler.javascript

object StringOps extends s2js.runtime.scala.collection.SeqCompanion
{
    def empty = new StringOps("")

    @javascript("return self.fromJsArray(xs.getInternalJsArray());")
    def apply(xs: Any*): Any = null
}

class StringOps(val x: java.lang.String) extends s2js.runtime.scala.collection.Seq
{
    initializeInternalJsArray(x)

    def newInstance = StringOps.empty

    @javascript("""self.setInternalJsArray(value.split(''))""")
    def initializeInternalJsArray(value: String) {}

    @javascript("return self.getInternalJsArray().join();")
    def repr: String = ""

    @javascript("return self.x == 'true';")
    def toBoolean: Boolean = false

    @javascript("return parseInt(self.x);")
    def toByte: Byte = 0

    @javascript("return parseInt(self.x);")
    def toShort: Short = 0

    @javascript("return parseInt(self.x);")
    def toInt: Int = 0

    @javascript("return parseInt(self.x);")
    def toLong: Long = 0

    @javascript("return parseFloat(self.x);")
    def toFloat: Float = 0

    @javascript("return parseFloat(self.x);")
    def toDouble: Double = 0.0

    override def toString = x
}
