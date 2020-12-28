/**
 * Block comment at top of file
 */
function /* comment */ foo(x) { // Inline comment at end of line
  // Inline comment on own line
  console.log(x);
  console.log("/* Block comment in a string literal */")
  console.log('/* Block comment in a string literal */')
  var a = "// Inline comment in a string literal";
  var b = '// Inline comment in a string literal';
}
// Inline comment at end of file