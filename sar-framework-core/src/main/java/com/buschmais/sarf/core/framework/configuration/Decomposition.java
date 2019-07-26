package com.buschmais.sarf.core.framework.configuration;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum Decomposition {
    @XmlEnumValue("flat") FLAT,
    @XmlEnumValue("deep") DEEP
}
