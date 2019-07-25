'use strict';

var convert = function (sarf) {
    console.log(sarf);
    var components = _.keyBy(_.map(sarf, function (neo4jRow) {
        var c = neo4jRow.entry[0];
        return {
            id : c.name,
            name: _.join(c.topWords),
            size: 50,
            parent: null,
            children: []
        };
    }), 'id');
    var types = _.keyBy(_.map(sarf, function (neo4jRow) {
        var t = neo4jRow.entry[1];
        if (t !== null && t.fqn) {
            return {
                id : t.fqn,
                name: t.name,
                size: 50,
                parent: null,
                children: []
            };
        }
    }), 'id');
    console.log(components);
    console.log(types);
    _.forEach(sarf, function (neo4jRow) {
        if (neo4jRow.entry[1]) {
            var c = neo4jRow.entry[0];
            var c1 = neo4jRow.entry[1];
            console.log(c);
            console.log(c1);
            var parent = components[c.name];
            console.log(parent);
            var child;
            if (c1.fqn) {
                child = types[c1.fqn];
            } else {
                child = components[c1.name];
            }
            console.log(child);
            if (parent.children.indexOf(child) === -1) {
                parent.children.push(child);
                child.parent = parent;
            }
        }
    });
    var rootComponents = _.filter(_.values(components), function (component) {
        return !component.parent;
    });
    return {
        name: "SARF",
        children: rootComponents
    }
};