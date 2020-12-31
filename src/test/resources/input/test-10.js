inspect: function(useDoubleQuotes) {
  var escapedString = this.gsub(/[\x00-\x1f\\]/, function(match) {
    var character = String.specialChar[match[0]];
    return character ? character : '\\u00' + match[0].charCodeAt().toPaddedString(2, 16);
  });
  if (useDoubleQuotes) return '"' + escapedString.replace(/"/g, '\\"') + '"';
  return "'" + escapedString.replace(/'/g, '\\\'') + "'";
},
toJSON: function() {
  return this.inspect(true);
},
unfilterJSON: function(filter) {
  return this.sub(filter || Prototype.JSONFilter, '#{1}');
},