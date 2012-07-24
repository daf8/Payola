package cz.payola.web.client.views

import cz.payola.web.client.View
import cz.payola.web.client.views.elements._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable

class VertexInfoTable(values: mutable.HashMap[String, Seq[String]]) extends ComposedView
{

    def createSubViews : Seq[View] = {

        val buffer = new ArrayBuffer[ListItem]()

        var even = true
        values.foreach{ x =>
            val innerList = x._2.map{ string =>
                new ListItem(List(new Text(string)))
            }

            buffer += new ListItem(List(new Text(x._1),new UnorderedList(innerList)),"badge "+(if(even){"badge-info"}))
            even = !even
        }

        List(new UnorderedList(buffer,"span5 unstyled well"))
    }

}
