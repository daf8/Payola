package s2js.adapters.js.dom

abstract class Range {
    def insertNode(n: Node)

    def surroundContents(n: Node)
}
