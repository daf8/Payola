package cz.payola.data.squeryl.entities

import cz.payola.data.squeryl._
import java.sql.Timestamp

object TransformerResult extends EntityConverter[TransformerResult]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[TransformerResult] = {
        entity match {
            case e: TransformerResult => Some(e)
            case e: cz.payola.common.entities.TransformerResult =>
                Some(new TransformerResult(e.transformerId, e.owner.map(User(_)), e.evaluationId,
                    e.verticesCount, new Timestamp(e.touched.getTime())))
            case _ => None
        }
    }
}

class TransformerResult (TransformerID: String, o: Option[User], EvaluationID: String,
    VerticesCount: Int, Touched: java.sql.Timestamp)
    (implicit val context: SquerylDataContextComponent)
    extends cz.payola.domain.entities.TransformerResult(TransformerID, o, EvaluationID, VerticesCount, Touched)
    with Entity with OptionallyOwnedEntity
{ }