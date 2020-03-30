/*
 *  A tree of connected nodes
 *
 *  Each node has an index >= 0
 *  and an odd value of n = 2*index+1
 *  and a step towards its successor.
 *
 *  A succ always exists.
 *  pred and sib are calculated on demand.
 *
 *  relations are:
 *
 *  (2 * succ.index +1) * 2**step == 3 * index + 2;
 *
 *  sib.index = 4 * index + 2;
 *
 */

function Tree() {

    function Node(succ, step) {
        this.step = step;
        this.max = this;

        if(succ) {
            this.succ = succ;
            this.len = succ.len+1;
            this.index = ((2*succ.index+1) * (1<<step) - 2) / 3;
            this.path = succ.path.concat(".").concat(step);
            this.sum = succ.sum + step;
            if(succ.max.index>this.index)
                this.max = succ.max;
            this._fac = undefined;
        } else {
            // root node(0)
            this.succ = this;
            this._pred = this;
            this.len = 0;
            this.index = 0;
            this.path = "1";
            this.sum = 1;
            this._fac = 1;
        }

        this.n = 2*this.index+1;

        this.pred = function(i) {

            var mod = this.index%3;

            if(mod==1)
                return undefined;

            if(i>0)
                return this.pred(i-1).sibl();

            var pred = this._pred;
            if(!pred) {
                var step = mod ? 0 : 1;
                pred = new Node(this, step);
                this._pred = pred;
            }

            return pred;
        }

        this.sibl = function() {
            var sibl = this._sibl;
            if(!sibl) {
                sibl = new Node(this.succ, this.step+2);
                this._sibl = sibl;
            }
            return sibl;
        }

        this.fac = function() {
            var fac = this._fac;
            if(!fac) {
                fac = 1;
                for(i=1; i<this.index; i++) {
                    if(this.contains(i))
                        ++fac;
                }
                this._fac = fac;
            }
            return fac;
        }

        this.contains = function(i) {
            i = 2*i+1;
            do {

                if(i==this.n)
                    return true;

                i = (3*i+1)/2;
                while((i%2)==0)
                    i = i/2;

            } while(i>1)

            return false;
        }

        this.succeeds = function(node) {
            if(node==undefined)
                return false;

            if(node==this)
                return true;

            if(this.succ==this)
                return false;

            return this.succ.succeeds(node);
        }

        this.toString = function() {
            return this.n.toString().concat('(').concat(this.len).concat(':').concat(this.path).concat(')');
        }
    }

    this.root = new Node(undefined, 1);

    this.node = function(index) {

        if(index==0)
            return this.root;

        var k = 3*index + 2;
        var step = 0;
        while(k>1 && !(k%2)) {
            ++step;
            k>>=1;
        }

        // to index
        k = (k-1)/2;

        step >>= 1;

        return this.node(k).pred(step);
    }

    this.inode = function(index) {

        if(index == undefined)
            return null;

        if(index%2!=1)  // even
            return null;
            
        return this.node((index-1)/2);
    }
}
