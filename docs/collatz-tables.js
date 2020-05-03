function L(n) {
    let L=0;
    while(n%2===0) {
        ++L;
        n /= 2;
    }

    return L;
}

function G(l) {
    return (4<<l)/3;
}

function table1(n) {

  const table = document.getElementById("table1");

  function row(n) {

      const row = table.insertRow();

      function cell(value) {
          const c=row.insertCell();
          c.style.textAlign = "right";

          if(value != undefined)
              c.textContent=value;
          return c;
      }

      const m=3*n+1;
      const l = L(m);
      const next = m>>l;

      cell(n);
      cell(m);
      cell(n.toString(2));
      
      const del = document.createElement("del");
      const tmp = document.createElement("span");
      tmp.style.color = "red";

      del.append("0");
      for(let i=0; i<l-1; ++i)
        tmp.append("0");

      del.append(tmp);

      cell().append(next.toString(2), del);

      cell(l-1).style.color = "red";

      return next;
  }

  while(n>1) {
    n = row(n);
  }

  row(1);
}

function table2(k) {

  let table = document.getElementById("table2");

  function row(k) {

      let row = table.insertRow();

      function cell(value) {
          let c=row.insertCell();
          c.style.textAlign = "right";

          if(value !== undefined)
              c.textContent=value;
          return c;
      }

      let n=2*k+1;

      let m=3*k+2;
      let l = L(m);

      let next = m>>(l+1);

      cell(n);
      cell(k);
      cell(m);

      cell().append(k.toString(2));

      let bits = document.createElement("span");
      bits.style.color ="blue";
      if(next>0) {
          bits.append(next.toString(2));
      }

      let del = document.createElement("del");
      del.append("1");

      let tail = document.createElement("span");
      tail.style.color = "red";

      for(let i=0; i<l; ++i)
          tail.append("0");

      del.append(tail);

      cell().append(bits, del);

      cell(l).style.color = "red";

      cell(next).style.color ="blue";

      return next;
  }

  while(k>0) {
    k = row(k);
  }

  row(0);
}

function table3(N) {

  let table = document.getElementById("table3");

  function row(k) {
    let row = table.insertRow();

    function cell(value) {
      let c=row.insertCell();
      if(value != undefined)
          c.textContent=value;
      return c;
    }

    let kn = 3*k+2;
    let l=L(kn);
    kn >>= l+1;

    cell(k);
    cell(kn);
    seq(cell(), k);
  }

  function seq(cell, k) {

    function span(l, k, kn) {
        cell.append('\u00a0');
        const span =  document.createElement("span");
        span.textContent = l.toString();
        span.title = k.toString() + '\u2192' + kn.toString();
        cell.append(span);
    }

    if(k<1) {
        span(1,0,0);
    } else {

        let kn = 3*k+2;
        let l=L(kn);
        kn >>= l+1;

        span(l,k,kn);
        if(kn>0) {
          seq(cell, kn);
        }
    }
  }

  for (let k = 0; k < N; k++) {
    row(k);
  }
}

function table4(N) {

  let table = document.getElementById("table4");

  function row(k) {
    let row = table.insertRow();

    function cell(value) {
      let c=row.insertCell();

      if(value != undefined)
          c.textContent=value;
      return c;
    }

    function seq(k) {
      let c = cell();
      c.style.textAlign = "left";

      function sq(k) {

        function span(l, k) {
            c.append('\u00a0');
            const span =  document.createElement("span");
            span.textContent = l.toString();
            span.title = k.toString() + '\u2192' + kn.toString();
            c.append(span);
            return span;
        }

        if(k<1) {
            return span(1,0,0);
        } else {

            let kn = 3*k+2;
            let l=L(kn);
            kn >>= l+1;

            if(kn>0) {
              sq(kn);
            }

            return span(l,kn,k);
        }
      }

      return sq(k);
    }

    let kn = 3*k+2;
    let l=L(kn);
    kn >>= l+1;
    const color = l%2==0 ? "red" : "green";

    cell(k);
    cell(kn).style.color = color;
    seq(k).style.color = color;
  }

  for (let k = 0; k < N; k++) {
    row(k);
  }
}

function table5(N) {

  let table = document.getElementById("table5");

  function row(k) {
    let row = table.insertRow();

    function cell(value) {
      let c=row.insertCell();

      if(value != undefined)
          c.textContent=value;
      return c;
    }
  }
}

window.onload = function() {
table1(9);
table2(4);
table3(12);
table4(12);
}

