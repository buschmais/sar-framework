//*******************************************************************
//  CREATE MATRIX AND MAP
//*******************************************************************
updateData();
function updateData(parent) {
    var x = function (data) {
        var mpr = jsonChordMpr(data);
        mpr
            .addValuesToMap('name', parent)
            .setFilter(function (row, a, b) {
                var res = false;
                if (row.name === a.name) {
                    // check if imports of a contains b
                    _.each(row.dependencies, function (i) {
                        if (i.name === b.name) res = true;
                    })
                }
                return res;
            })
            .setAccessor(function (recs, a, b) {
                if ('undefined' === typeof recs[0]) return 0;
                if (!recs[0]) return 0;
                var res = 0;
                if (recs[0].name === a.name) {
                    // check if imports of a contains b
                    _.each(recs[0].dependencies, function (i) {
                        if (i.name === b.name) res =  +i.weight;
                    })
                }
                return res;
            });
        drawChords(mpr.getMatrix(), mpr.getMap());
    };

    if ('undefined' === typeof parent) {
        d3.json("chord-data.json", x)
    } else {
        x(parent.children);
    }
}
//*******************************************************************
//  DRAW THE CHORD DIAGRAM
//*******************************************************************
function drawChords (matrix, mmap) {
    var w = 980, h = 800, radius = h / 2 - 110;
    var fill = d3.scale.category20();
    var chord = d3.layout.chord()
        .padding(.02)
        .sortSubgroups(d3.descending)
        .sortChords(d3.descending);
    var arc = d3.svg.arc()
        .innerRadius(radius)
        .outerRadius(radius + 60);
    var svg = d3.select("#chord").append("svg")
        .attr("width", w)
        .attr("height", h)
        .append("svg:g")
        .attr("id", "circle")
        .attr("transform", "translate(" + w / 2 + "," + h / 2 + ")");
    svg.append("circle")
        .attr("r", radius + 50);
    var rdr = chordRdr(matrix, mmap);
    chord.matrix(matrix);
    var g = svg.selectAll("g.group")
        .data(chord.groups())
        .enter().append("svg:g")
        .attr("class", "group")
        .on("mouseover", mouseover)
        .on("contextmenu", zoomOut)
        .on("dblclick",zoom)
        .on("mouseout", function (d) { d3.select("#tooltip").style("visibility", "hidden") });
    g.append("svg:path")
        .style("stroke", "black")
        .style("fill", function(d) { return fill(rdr(d).name); })
        .attr("d", arc);
    g.append("svg:text")
        .each(function(d) { d.angle = (d.startAngle + d.endAngle) / 2; })
        .attr("dy", ".35em")
        .style("font-family", "helvetica, arial, sans-serif")
        .style("font-size", "9px")
        .attr("text-anchor", function(d) { return d.angle > Math.PI ? "end" : null; })
        .attr("transform", function(d) {
            return "rotate(" + (d.angle * 180 / Math.PI - 90) + ")"
                + "translate(" + (radius + 6) + ")"
                + (d.angle > Math.PI ? "rotate(180)" : "");
        })
        .text(function(d) { return rdr(d).name; });
    var chordPaths = svg.selectAll("path.chord")
        .data(chord.chords())
        .enter().append("svg:path")
        .attr("class", "chord")
        .style("stroke", function(d) { return d3.rgb(fill(rdr(d).sname)).darker(); })
        .style("fill", function(d) { return fill(rdr(d).sname); })
        .attr("d", d3.svg.chord().radius(radius))
        .on("mouseover", function (d) {
            d3.select("#tooltip")
                .style("visibility", "visible")
                .html(chordTip(rdr(d)))
                .style("top", function () { return (d3.event.pageY + 10)+"px"})
                .style("left", function () { return (d3.event.pageX  + 10)+"px";})
        })
        .on("mouseout", function (d) { d3.select("#tooltip").style("visibility", "hidden") });
    function chordTip (d) {
        var p = d3.format(".1%");
        var tip = "Dependency Info:<br/>"
            +  d.sname + " depends on " + d.tname + ":" + d.svalue + "<br/>"
            + p(d.svalue/d.stotal) + " of " + d.sname + "'s Total (" + d.stotal + ")<br/>"
            + p(d.svalue/d.mtotal) + " of Component Total (" + d.mtotal + ")";
        if (d.tname !== d.sname) {
            tip += "<br/><br/>"
                + d.tname + " depends on " + d.sname + ": " + d.tvalue + "<br/>"
                + p(d.tvalue / d.ttotal) + " of " + d.tname + "'s Total (" + d.ttotal + ")<br/>"
                + p(d.tvalue / d.mtotal) + " of Component Total (" + d.mtotal + ")";
        }
        return tip;
    }
    function groupTip (d) {
        var p = d3.format(".1%");
        return d.name  + "<br/>"
            + "Number of Dependencies: " + d.dependencies + "<br/>"
            + "Number of Contained Types: " + d.size + "<br/>"
            + "Description: " + d.description + "<br/>"
            + p(d.dependencies/d.mtotal) + " of Matrix Total (" + d.mtotal + ")"
    }
    function mouseover(d, i) {
        d3.select("#tooltip")
            .style("visibility", "visible")
            .html(groupTip(rdr(d)))
            .style("top", function () { return (d3.event.pageY + 10)+"px"})
            .style("left", function () { return (d3.event.pageX + 10)+"px";})
        chordPaths.classed("fade", function(p) {
            return p.source.index != i
                && p.target.index != i;
        });
    }

    function zoom(d, i) {
        d3.select("#chord").select("svg").remove();
        updateData(rdr(d));
    }

    function zoomOut(d, i) {
        d3.event.preventDefault();
        if ('undefined' !== typeof rdr(d).parent) {
            d3.select("#chord").select("svg").remove();
            updateData(rdr(d).parent.parent);
        }
    }
}