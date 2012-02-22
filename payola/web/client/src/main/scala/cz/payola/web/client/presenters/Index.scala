package cz.payola.web.client.presenters

import s2js.adapters.js.browser._
import cz.payola.web.client.views.graph.GraphView
import cz.payola.common.rdf.Graph
import cz.payola.web.shared.GraphFetcher
import cz.payola.web.client.model.graph.{SimpleGraph, SimpleEdge, SimpleIdentifiedVertex}

class Index
{
    val graphModel = initGraph()

    val graphView = new GraphView(graphModel, document.getElementById("canvas-holder"))

    def init() {
        graphView.init()
        graphView.redrawAll();
    }

    def initGraph(): Graph = {
        // TODO retrieve the graph from the server using following call when RPC and server side is done.
        // GraphFetcher.getInitialGraph

        val v0 = new SimpleIdentifiedVertex("0")
        val v1 = new SimpleIdentifiedVertex("1")
        val v2 = new SimpleIdentifiedVertex("2")
        val v3 = new SimpleIdentifiedVertex("3")
        val v4 = new SimpleIdentifiedVertex("4")
        val v5 = new SimpleIdentifiedVertex("5")
        val v6 = new SimpleIdentifiedVertex("6")
        /*val v7 = new SimpleIdentifiedVertex("7")
        val v8 = new SimpleIdentifiedVertex("8")
        val v9 = new SimpleIdentifiedVertex("9")
        val v10 = new SimpleIdentifiedVertex("10")
        val v11 = new SimpleIdentifiedVertex("11")
        val v12 = new SimpleIdentifiedVertex("12")
        val v13 = new SimpleIdentifiedVertex("13")
        val v14 = new SimpleIdentifiedVertex("14")
        val v15 = new SimpleIdentifiedVertex("15")
        val v16 = new SimpleIdentifiedVertex("16")
        val v17 = new SimpleIdentifiedVertex("17")
        val v18 = new SimpleIdentifiedVertex("18")
        val v19 = new SimpleIdentifiedVertex("19")*/

        /*val e0 = new SimpleEdge("0", v0, v1)
        val e1= new SimpleEdge("1", v0, v2)
        val e2 = new SimpleEdge("2", v0, v3)
        val e3 = new SimpleEdge("3", v1, v4)
        val e4 = new SimpleEdge("4", v1, v5)
        val e5 = new SimpleEdge("5", v0, v6)
        val e6 = new SimpleEdge("6", v0, v4)
        val e7 = new SimpleEdge("7", v0, v5)
        val e8 = new SimpleEdge("8", v1, v6)*/
        val e0 = new SimpleEdge("0", v0, v1)
        val e1 = new SimpleEdge("1", v0, v2)
        val e2 = new SimpleEdge("2", v1, v3)
        val e3 = new SimpleEdge("3", v1, v4)
        val e4 = new SimpleEdge("4", v1, v5)
        val e5 = new SimpleEdge("5", v2, v4)
        val e6 = new SimpleEdge("6", v2, v6)
        /*val e0 = new SimpleEdge("0", v0, v1)
        val e1 = new SimpleEdge("1", v0, v2)
        val e2 = new SimpleEdge("2", v0, v9)
        val e3 = new SimpleEdge("3", v0, v11)
        val e4 = new SimpleEdge("4", v0, v16)
        val e5 = new SimpleEdge("5", v1, v5)
        val e6 = new SimpleEdge("6", v1, v6)
        val e7 = new SimpleEdge("7", v2, v3)
        val e8 = new SimpleEdge("8", v2, v5)
        val e9 = new SimpleEdge("9", v2, v6)
        val e10 = new SimpleEdge("10", v2, v8)
        val e11 = new SimpleEdge("11", v3, v4)
        val e12 = new SimpleEdge("12", v3, v5)
        val e13 = new SimpleEdge("13", v3, v11)
        val e14 = new SimpleEdge("14", v4, v7)
        val e15 = new SimpleEdge("15", v4, v8)
        val e16 = new SimpleEdge("16", v4, v11)
        val e17 = new SimpleEdge("17", v5, v6)
        val e18 = new SimpleEdge("18", v5, v12)
        val e19 = new SimpleEdge("19", v6, v7)
        val e20 = new SimpleEdge("20", v6, v9)
        val e21 = new SimpleEdge("21", v7, v9)
        val e22 = new SimpleEdge("22", v8, v9)
        val e23 = new SimpleEdge("23", v8, v16)
        val e24 = new SimpleEdge("24", v9, v10)
        val e25 = new SimpleEdge("25", v9, v13)
        val e26 = new SimpleEdge("26", v9, v15)
        val e27 = new SimpleEdge("27", v10, v11)
        val e28 = new SimpleEdge("28", v10, v12)
        val e29 = new SimpleEdge("29", v11, v13)
        val e30 = new SimpleEdge("30", v11, v18)
        val e31 = new SimpleEdge("31", v11, v19)
        val e32 = new SimpleEdge("32", v12, v13)
        val e33 = new SimpleEdge("33", v13, v14)
        val e34 = new SimpleEdge("34", v13, v19)
        val e35 = new SimpleEdge("35", v15, v16)
        val e36 = new SimpleEdge("36", v15, v17)
        val e37 = new SimpleEdge("37", v16, v17)*/

        new SimpleGraph(
            List(
                v0, v1, v2, v3, v4, v5, v6 //, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19
            ),
            List(
                e0, e1, e2, e3, e4, e5, e6 /*, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17, e18, e19, e20, e21,
                e22, e23, e24, e25, e26, e27, e28, e29, e30, e31, e32, e33, e34, e35, e36, e37*/
            )
        )
    }
}