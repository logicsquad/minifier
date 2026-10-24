s = `line 1
line 2`;
x = `a ${ `b  c` } d`;
x = `${ secure ? `https://` : `http://` }${host}`;
x = html`<ul>${items.map(i => html`<li>${i} isn't</li>`)}</ul>`;
x = `a ${ "`" } b`;
x = `a ${ b } c`;
x = `${ {a: 1}.a }`;
x = `\${a} \` $ {b} $`;
