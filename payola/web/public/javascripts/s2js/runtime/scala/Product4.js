goog.provide('scala.Product4');
goog.require('scala.IndexOutOfBoundsException');
goog.require('scala.Product');
goog.require('scala.Some');
scala.Product4 = function() {
var self = this;
goog.object.extend(self, new scala.Product());
};
scala.Product4.prototype.productArity = function() {
var self = this;
return 4;
};
scala.Product4.prototype.productElement = function(n) {
var self = this;
return (function($selector_1) {
if ($selector_1 === 0) {
return self._1();
}
if ($selector_1 === 1) {
return self._2();
}
if ($selector_1 === 2) {
return self._3();
}
if ($selector_1 === 3) {
return self._4();
}
if (true) {
return (function() {
throw new scala.IndexOutOfBoundsException(n.toString());
})();
}
})(n);
};
scala.Product4.prototype.metaClass_ = new s2js.MetaClass('scala.Product4', [scala.Product]);
scala.Product4.unapply = function(x) {
var self = this;
return new scala.Some(x);
};
scala.Product4.metaClass_ = new s2js.MetaClass('scala.Product4', []);
