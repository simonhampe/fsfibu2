<?xml version='1.0' encoding='UTF-8'?><!-- -*- indent-tabs-mode: nil -*- -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml"
                version="1.0">
<!-- Diese Format stellt das Kassenbuch im Format dar, wie es auch in den alten
ExcelTabellen stand. Makrofelder werden dabei ausgelassen. Um die so generierte
.csv-Datei zu verwenden, sollte man sie separat laden und die benötigten Spalten dann 
einzeln in die tatsächliche Kassenbuch-Datei kopieren -->

<xsl:output method="text"/>
<!-- Eliminiert die überzähligen text-Nodes, die von SAXReader generiert wurden-->
<xsl:template match="text()"></xsl:template>

<xsl:template match="einträge">
	<xsl:apply-templates>
	<!-- Sortieren... -->
	<!-- Diese Konfiguration soll genauso sortieren wie compareTo in FsEintrag -->
	<!-- Da sie jedoch getrennt implementiert wurde, ist das nicht garantiert -->
	<xsl:sort select="datum"/>
	<xsl:sort select="echtdatum"/>
	<xsl:sort select="concat(name,kategorie,gruppe,infos)"/>
	<xsl:sort select="wert"/>
	<xsl:sort select="rnummer"/>
	<xsl:sort select="auszug"/>
	<xsl:sort select="istkontobewegung"/>
	<xsl:sort select="@uid"/>
	</xsl:apply-templates>
</xsl:template>  


<xsl:template match="eintrag">
	<xsl:value-of select="name"/>
	<!-- Formatiere Rechnungsnr und Auszug -->
	<xsl:if test="string-length(concat(rnummer,auszug)) > 0">(</xsl:if>
	<xsl:if test="string-length(rnummer) > 0">
		<xsl:value-of select="rnummer"/>
		<xsl:text> </xsl:text>
		<xsl:if test="string-length(auszug) > 0">
			<xsl:text>, </xsl:text>
		</xsl:if>
	</xsl:if>
	<xsl:if test="string-length(auszug) > 0"><xsl:value-of select="auszug"/></xsl:if>
	<xsl:if test="string-length(concat(rnummer,auszug)) > 0">)</xsl:if>
	<xsl:call-template name="separator"/>
	<xsl:value-of select="datum"/>
	<xsl:call-template name="separator"/>
	<!-- Formatiere Bank und Kasse -->
	<xsl:choose>
		<xsl:when test="istkontobewegung='true'">
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:value-of select="wert"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="wert"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
		</xsl:otherwise>
	</xsl:choose>
	<!--  Formatiere Kategorie. Da die Kategorie in FsFiBu vllt leicht
	unterschiedlich geschrieben ist, geschieht die Kategorienzuweisung 
	per lockerem Vergleich, d.h. ausreichende Übereinstimmung wird als 
	Gleichheit angesehen -->
	<xsl:choose>
		<xsl:when test="contains(kategorie,'Getränke')">
			<xsl:value-of select="wert"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
		</xsl:when>
		<xsl:when test="contains(kategorie,'Veranstaltungen')">
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:value-of select="wert"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
		</xsl:when>
		<xsl:when test="contains(kategorie,'Fachschaft')">
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:value-of select="wert"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
			<xsl:call-template name="separator"/>
		</xsl:when>
	</xsl:choose>
	<!-- Gib Gruppennamen an  -->
	<xsl:value-of select="gruppe"/>
	<xsl:call-template name = "absatz"/>
</xsl:template>

<!-- Formate -->
<xsl:template name="absatz">
	<xsl:text>
</xsl:text>
</xsl:template>
<xsl:template name="separator">
	<xsl:text>;</xsl:text>
</xsl:template>

</xsl:stylesheet>