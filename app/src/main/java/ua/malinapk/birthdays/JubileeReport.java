package ua.malinapk.birthdays;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.text.Collator;
import java.util.*;
import java.util.zip.*;

/** Generates a separate XLSX containing only jubilee employees, not the source workbook. */
final class JubileeReport {
 private static final String XML="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
 private static final String NS="http://schemas.openxmlformats.org/spreadsheetml/2006/main";
 private static final String REL="http://schemas.openxmlformats.org/officeDocument/2006/relationships";
 private static final DateTimeFormatter DATE=DateTimeFormatter.ofPattern("dd.MM.uuuu");
 private static String escape(String s){StringBuilder b=new StringBuilder();s.codePoints().forEach(c->{switch(c){case '&':b.append("&amp;");break;case '<':b.append("&lt;");break;case '>':b.append("&gt;");break;default:if(c==9||c==10||c==13||(c>=32&&c<=0xd7ff)||(c>=0xe000&&c<=0xfffd)||(c>=0x10000&&c<=0x10ffff))b.appendCodePoint(c);}});return b.toString();}
 private static void entry(ZipOutputStream z,String path,String text)throws IOException{z.putNextEntry(new ZipEntry(path));z.write((XML+text).getBytes(StandardCharsets.UTF_8));z.closeEntry();}
 private static void cell(StringBuilder s,int col,int row,String val,boolean numeric){s.append("<c r=\"").append((char)('A'+col)).append(row).append("\"");if(row==1)s.append(" s=\"1\"");if(numeric)s.append("><v>").append(val).append("</v></c>");else s.append(" t=\"inlineStr\"><is><t xml:space=\"preserve\">").append(escape(val)).append("</t></is></c>");}
 static byte[] create(List<Person> source,int year)throws IOException{
  List<Person> list=new ArrayList<>();for(Person p:source)if(p.jubilee(year))list.add(p);
  Collator collator=Collator.getInstance(Locale.forLanguageTag("uk-UA"));collator.setStrength(Collator.SECONDARY);
  list.sort(Comparator.comparing((Person p)->p.birthday(year)).thenComparing(Person::name,collator));
  StringBuilder s=new StringBuilder("<worksheet xmlns=\""+NS+"\"><dimension ref=\"A1:H"+(list.size()+1)+"\"/><sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews><cols><col min=\"1\" max=\"3\" width=\"24\" customWidth=\"1\"/><col min=\"4\" max=\"5\" width=\"32\" customWidth=\"1\"/><col min=\"6\" max=\"8\" width=\"21\" customWidth=\"1\"/></cols><sheetData><row r=\"1\">");
  String[] headers={"Прізвище","Ім’я","По батькові","Посада","Підрозділ","Дата народження","Дата ювілею","Вік у "+year+" році"};for(int i=0;i<headers.length;i++)cell(s,i,1,headers[i],false);s.append("</row>");
  int r=2;for(Person p:list){s.append("<row r=\"").append(r).append("\">");String[] a={p.last,p.first,p.middle,p.job,p.unit,p.born.format(DATE),p.birthday(year).format(DATE),Integer.toString(p.ageIn(year))};for(int i=0;i<a.length;i++)cell(s,i,r,a[i],i==7);s.append("</row>");r++;}s.append("</sheetData><autoFilter ref=\"A1:H").append(r-1).append("\"/></worksheet>");
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ZipOutputStream z=new ZipOutputStream(bytes)){
   entry(z,"[Content_Types].xml","<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/><Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/></Types>");
   entry(z,"_rels/.rels","<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\""+REL+"/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");
   entry(z,"xl/workbook.xml","<workbook xmlns=\""+NS+"\" xmlns:r=\""+REL+"\"><sheets><sheet name=\"Ювіляри "+year+"\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
   entry(z,"xl/_rels/workbook.xml.rels","<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\""+REL+"/worksheet\" Target=\"worksheets/sheet1.xml\"/><Relationship Id=\"rId2\" Type=\""+REL+"/styles\" Target=\"styles.xml\"/></Relationships>");
   entry(z,"xl/styles.xml","<styleSheet xmlns=\""+NS+"\"><fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font><font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font></fonts><fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill><fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF235D45\"/><bgColor indexed=\"64\"/></patternFill></fill></fills><borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs><cellXfs count=\"2\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/><xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/></cellXfs><cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles></styleSheet>");
   entry(z,"xl/worksheets/sheet1.xml",s.toString());
  }return bytes.toByteArray();
 }
}
