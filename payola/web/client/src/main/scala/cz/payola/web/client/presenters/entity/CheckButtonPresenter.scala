package cz.payola.web.client.presenters.entity

import s2js.adapters.html
import cz.payola.web.shared._
import cz.payola.common.entities._
import cz.payola.web.client._
import cz.payola.web.client.views.entity._
import cz.payola.web.client.events._
import cz.payola.common.Entity
import cz.payola.web.client.views.bootstrap.modals.AlertModal

/**
 * A presenter which controls the behaviour of a check button. The button is created by the presenter and placed
 * into the passed viewElement.
 * @param viewElement A placeholder for the created button.
 * @param entityId Id of the entity which could be checked.
 * @param viewToBlock Which view has to be blocked while the button is working.
 */
class CheckButtonPresenter(
    val viewElement: html.Element,
    val entityId: String,
    val viewToBlock: Option[View] = None)
    extends Presenter
{
    val publicityChanged = new SimpleUnitEvent[Boolean]

    private val view = new CheckButton(Payola.model.analysisModel.getAccessibleToUserById(None,entityId).get.checked)

    def initialize() {
        view.checkButton.mouseClicked += onCheckButtonClicked _
        view.render(viewElement)
    }

    private def onCheckButtonClicked(e: EventArgs[_]): Boolean = {
        //view.setIsEnabled(false)
        //val newPublicity = !view.isPublic
        //SharingData.setEntityPublicity(entityClassName, entityId, newPublicity) { () =>
        //    view.isPublic = newPublicity
        //    view.setIsEnabled(true)
        //    publicityChanged.triggerDirectly(newPublicity)
        //}(fatalErrorHandler(_))
        false
    }

    /*private def onShareButtonClicked(granteeClassName: String, granteeClassNameText: String) {
        blockView("Fetching share data...")
        SharingData.getEntityGrantees(entityClassName, entityId, granteeClassName) { grantees =>
            unblockView()

            val shareModal = new ShareModal(entityName, granteeClassNameText, grantees)
            shareModal.granteeSearching += { e =>
                SharingData.searchPotentialGrantees(granteeClassName, e.searchTerm) {
                    e.successCallback(_)
                }(fatalErrorHandler(_))
            }
            shareModal.confirming += { e =>
                blockView("Sharing...")
                val granteeIds = shareModal.granteeSelection.field.value
                SharingData.shareEntity(entityClassName, entityId, granteeClassName, granteeIds) { () =>
                    unblockView()
                    AlertModal.display("Success", "The entity was successfully shared to selected %ss.".format(granteeClassNameText),
                        "alert-success", Some(4000))
                }(fatalErrorHandler(_))
                true
            }
            shareModal.render()

        }(fatalErrorHandler(_))
    }*/

    private def blockView(message: String) {
        if (viewToBlock.isDefined) {
            viewToBlock.get.block(message)
        } else {
            blockPage(message)
        }
    }

    private def unblockView() {
        if (viewToBlock.isDefined) {
            viewToBlock.get.unblock()
        } else {
            unblockPage()
        }
    }
}

object CheckButtonPresenter
{
    def apply(viewElement: html.Element, entity: ShareableEntity, viewToBlock: Option[View]): CheckButtonPresenter = {
        new CheckButtonPresenter(viewElement, entity.id, viewToBlock)
    }
}
