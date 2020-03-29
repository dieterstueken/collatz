function L(n) {
    let L=0;
    while(n%2===0) {
        ++L;
        n /= 2;
    }

    return L;
}

function table1(n) {

  let table = document.getElementById("table1");

  function row(n) {

      let row = table.insertRow();

      function cell(value) {
          let c=row.insertCell();
          c.style.textAlign = "right";

          if(value != undefined)
              c.textContent=value;
          return c;
      }

      let m=3*n+1;
      let l = L(m);
      let next = m>>l;

      cell(n);
      cell(m);
      cell(n.toString(2));
      
      let del = document.createElement("del")
      for(let i=0; i<l; ++i)
        del.append("0");

      cell().append(next.toString(2), del);

      cell(l-1);

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

          if(value != undefined)
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
      cell(k.toString(2));

      let del = document.createElement("del")
      del.append("1");

      for(let i=0; i<l; ++i)
        del.append("0");

      cell().append(next>0?next.toString(2):"", del);

      cell(l);

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
      c.style.textAlign = "right";

      if(value != undefined)
          c.textContent=value;
      return c;
    }

    let kn = 3*k+2;
    let l=L(kn);
    kn >>= l+1;

    let c = cell(k);

    c = cell(kn);
    seq(cell(), k);
  }

  function seq(cell, k) {

    function span(l, k) {
        let span =  document.createElement("span");
        span.textContent = l.toString();
        span.title = k.toString();
        cell.append(span);
    }

    if(k<1) {
        span(1,0);
    } else {

        k = 3*k+2;
        let l=L(k);
        k >>= l+1;

        span(l,k);
        if(k>0) {
          cell.append(' ');
          seq(cell, k);
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
          c.style.textAlign = "right";

          if(value != undefined)
              c.textContent=value;
          return c;
      }

    let kn = 3*k+2;
    let l=L(kn);
    kn >>= l+1;

    let c = cell(k);

    switch(k%3) {
      case 0:
        c.style.color = "blue";
        break;
      case 2:
         c.style.color = "green";
         break;
    }

    c = cell(kn);
    if(kn>k)
      c.style.color = "red";   

    c = cell();
    c.style.textAlign = "left";
    seq(c, k);
  }

  function seq(cell, k) {

    function span(l, k, kn) {
        let span =  document.createElement("span");
        span.textContent = l.toString();
        span.title = k.toString();
        switch(k%3) {
            case 0:
                span.style.color = "blue";
                break;
            case 2:
                 span.style.color = "green";
                 break;
        }
        cell.append(span);
    }

    if(k<1) {
        span(1,0,0);
    } else {

        let kn = 3*k+2;
        let l=L(kn);
        kn >>= l+1;

        if(kn>0) {
          seq(cell, kn);
          cell.append(' ');
        }

        span(l,k, kn);
    }
  }

  for (let k = 0; k < N; k++) {
    row(k);
  }
}

window.onload = function() {
table1(9);
table2(4);
table3(10);
table4(40);
}

