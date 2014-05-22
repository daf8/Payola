package cz.payola.web.client.presenters.entity.plugins

import cz.payola.web.client.Presenter
import s2js.adapters.html
import cz.payola.web.client.models.Model
import cz.payola.web.client.views.entity.plugins._
import cz.payola.web.shared.managers.DataSourceManager
import cz.payola.common.ValidationException
import cz.payola.common.entities.plugins.DataSource
import cz.payola.common.entities._
import cz.payola.web.client.views.bootstrap.modals.AlertModal
import cz.payola.web.shared.PipelineData
import cz.payola.web.client.views.elements._

class DataSourcePipeline(val viewElement: html.Element, dataSourceID: String) extends Presenter
{
    def initialize() {
        blockPage("Creating pipelines...")
        PipelineData.getCompatibleTableWithDataSource(dataSourceID) { rows =>
            initializeView(rows)
            unblockPage()
        } { error =>
            fatalErrorHandler(error)
        }
    }

    private def initializeView(rowsString: List[List[String]]) {
        var rows: List[TableRow] = List()
        rowsString.map{ row =>
            rows ++= List(new TableRow(List(new TableCell(List(new Text(row(0)))),
                new TableCell(List(new Text(row(1)))), new TableCell(List(new Text(row(2)))))))
        }
        val table: Table = new Table(List(new TableHead(List(new TableHeadCell(List(new Text("Analyses"))),
            new TableHeadCell(List(new Text("Transformers"))), new TableHeadCell(List(new Text("Visualizers"))))),
            new TableBody(rows)), "table table-striped")
        table.render(viewElement)
    }

}
