package cz.payola.scala2json

import annotations._

class TestObjectClass(var str: String, i: Int) {
    var smth: String = "help"

    @JSONFieldName(name = "heaven") var hell: Double = 33.0d
    @JSONTransient var transient: Int = 33
}

object Tester {
    def main(args: Array[String]){
        val t: TestObjectClass = new TestObjectClass("Hello", 22)
        val s2json: JSONSerializer = new JSONSerializer(t)

        println(s2json.stringValue())
        
    }
}