package cz.payola.web.client.presenters.components

import cz.payola.web.client.views.elements._
import cz.payola.web.client.views.bootstrap._
import cz.payola.web.client.views.elements.form.fields._
import cz.payola.web.shared.DomainData
import s2js.compiler._
import s2js.adapters.html
import s2js.adapters.browser.`package`._

/**
 * A dialog used to create a plugin of an transformer.
 * @author Jiri Helmich
 */
class TransformerPluginDialog() extends Modal("Create a new plugin from an existing transformer", Nil, Some("OK"), Some("Cancel"), true, "choose-transformer-dialog")
{


    @javascript(""" return jQuery("#transformer").select2("val"); """)
    def getChosenTransformerID : String = ""

    @javascript(
        """
          jQuery("#transformer").select2({
              minimumInputLength: 1,
              multiple: false,
              query: function (query) {
                  var data = {results: []};
                  self.fetchTransformersByQuery(
                    query.term,
                    function(id, text){ data.results.push({id: id, text: text}); },
                    function(){ query.callback(data); }
                  );
              },
              initSelection : function (element) {
                  var data = [];
                  $(element.val().split(",")).each(function () {
                      var parts = this.split(":");
                      data.push({id: parts[0], text: parts[1]});
                  });
                  return data;
              }
            });

            jQuery(".select2-container").css('width','300px');
        """)
    def bindSelect {}

    def fetchTransformersByQuery(term: String, itemCallback: (String, String) => Unit, callback: () => Unit) {
        DomainData.searchAccessibleTransformers(term) { transformers =>
            transformers.map { u =>
                itemCallback(u.id, u.name)
            }
            callback()
        } { _ =>}
    }

    val placeholder = new Div(List(new Hidden("transformer", "", "Choose an transformer")))

    override val body = List(placeholder)

    override def render(parent: html.Element = document.body) {
        super.render(parent)
        bindSelect
    }
}
