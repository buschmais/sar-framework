//*******************************************************************
//  CHORD MAPPER
//*******************************************************************
function jsonChordMpr(data) {
    var mpr = {}, mmap = {}, n = 0,
        matrix = [], filter, accessor;

    mpr.setFilter = function (fun) {
        filter = fun;
        return this;
    };
    mpr.setAccessor = function (fun) {
        accessor = fun;
        return this;
    };
    mpr.getMatrix = function () {
        matrix = [];
        var i = 0;
        _.each(mmap, function (a) {
            if (!matrix[a.id]) matrix[a.id] = [];
            _.each(mmap, function (b) {
                var recs = _.filter(data, function (row) {
                    return filter(row, a, b);
                });
                matrix[a.id][b.id] = accessor(recs, a, b);
            });
        });
        return matrix;
    };
    mpr.getMap = function () {
        return mmap;
    };
    mpr.printMatrix = function () {
        _.each(matrix, function (elem) {
            console.log(elem);
        })
    };
    mpr.addToMap = function (value, info) {
        if (!mmap[value]) {
            mmap[value] = {name: value, id: n++, data: info}
        }
    };
    mpr.addValuesToMap = function (varName, parent, info) {
        _.map(data, function (entry) {
            if (!mmap[entry.name]) {
                mmap[entry.name] = {
                    name: entry.name,
                    id: n++,
                    description: entry.description,
                    size: entry.size,
                    children: entry.children,
                    parent: parent
                }
            }
        });
        return this;
    };
    return mpr;
}

//*******************************************************************
//  CHORD READER
//*******************************************************************
function chordRdr(matrix, mmap) {
    return function (d) {
        var i, j, s, t, g, m = {};
        if (d.source) {
            /*Chord*/
            i = d.source.index;
            j = d.target.index;
            s = _.where(mmap, {id: i});
            t = _.where(mmap, {id: j});
            m.sname = s[0].name;
            m.sdata = d.source.value;
            m.svalue = +d.source.value;
            m.stotal = _.reduce(matrix[i], function (k, n) {
                return k + n
            }, 0);
            m.tname = t[0].name;
            m.tdata = d.target.value;
            m.tvalue = +d.target.value;
            m.ttotal = _.reduce(matrix[j], function (k, n) {
                return k + n
            }, 0);
        } else {
            /*Component*/
            g = _.where(mmap, {id: d.index});
            m.name = g[0].name;
            m.description = g[0].description;
            m.size = g[0].size;
            m.dependencies = Math.round(d.value);
            m.children = g[0].children;
            m.parent = g[0].parent;
        }
        m.mtotal = _.reduce(matrix, function (m1, n1) {
            return m1 + _.reduce(n1, function (m2, n2) {
                return m2 + n2
            }, 0);
        }, 0);
        return m;
    }
}