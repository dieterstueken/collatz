function table3() {

  var table = document.getElementById("table3");

  function row(k) {
    var row = table.insertRow();

    function cell(value) {
      var cell=row.insertCell()
      //var span =  document.createElement("span");
      //cell.appendChild(span);
      cell.textContent=value;
      return cell;
    }

    var kn = 3*k+2;
    var l=0;

    while(kn%2==0) {
      kn /= 2;
      ++l;
    }

    kn -= 1;
    kn /= 2;

    cell(2*k+1).style.textAlign = "right";
    cell(k).style.textAlign = "right";
    cell(kn).style.textAlign = "right";
    cell(seq(k)).style.textAlign = "left";
  }

  function seq(k) {
    if(k<1)
        return "";

    k = 3*k+2;

    var l=0;
    while(k%2==0) {
        k /= 2;
        ++l
    }

     k -= 1;
     k /= 2;

    return l.toString().concat(' ').concat(seq(k));
  }

  for (var k = 0; k < 8; k++) {
    row(k);
  }
};

window.onload = table3;
