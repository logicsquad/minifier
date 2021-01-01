var options_for_droppable = {
  overlap:     options.overlap,
  containment: options.containment,
  tree:        options.tree,
  hoverclass:  options.hoverclass,
  onHover:     Sortable.onHover
}
var options_for_tree = {
  onHover:      Sortable.onEmptyHover,
  overlap:      options.overlap,
  containment:  options.containment,
  hoverclass:   options.hoverclass
}
// fix for gecko engine   
Element.cleanWhitespace(element); 