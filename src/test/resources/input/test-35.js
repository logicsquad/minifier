/*! Licence: kept */
/** Documentation: removed */
/* Plain: removed */
// Line: removed
var a = 1;
/*! Kept between statements */
var b = a /*! Kept in an expression */ / 2;
function f() {
    return /*!
     * Kept, with the line break in it that ends the statement
     */ a;
}
var c = b
/*! Kept between two statements that only a line break separates */
!function () {}()
