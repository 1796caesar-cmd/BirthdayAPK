package ua.malinapk.birthdays;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
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
 private static void cell(StringBuilder s,int col,int row,String val,boolean numeric){s.append("<c r=\"").append((char)('A'+col)).append(row).append("\"");s.append(row==1?" s=\"1\"":val.isEmpty()?" s=\"0\"":" s=\"2\"");if(numeric)s.append("><v>").append(val).append("</v></c>");else s.append(" t=\"inlineStr\"><is><t xml:space=\"preserve\">").append(escape(val)).append("</t></is></c>");}
 static byte[] create(List<Person> source,int year)throws IOException{return createFiltered(source,year,0,"",true,0,false);}
 static byte[] createMonth(List<Person> source,YearMonth month)throws IOException{return createFiltered(source,month.getYear(),month.getMonthValue(),"",false,0,false);}
 static byte[] createFiltered(List<Person> source,int year,int month,String unit,boolean jubilees,int sort,boolean split)throws IOException{
  List<Person> list=ExportFilter.select(source,year,month,unit,jubilees,sort);
  LinkedHashMap<String,List<Person>> groups=new LinkedHashMap<>();
  if(split){for(Person person:list){String name=person.unit.trim();String key=ExportFilter.key(name);String label=groups.keySet().stream().filter(k->ExportFilter.key(k).equals(key)).findFirst().orElse(name.isEmpty()?"Без підрозділу":name);groups.computeIfAbsent(label,k->new ArrayList<>()).add(person);}}
  if(groups.isEmpty()||!split){groups.clear();groups.put(jubilees?"Ювіляри "+year:"Іменинники "+year,list);}
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ZipOutputStream z=new ZipOutputStream(bytes)){
   StringBuilder types=new StringBuilder("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>");
   StringBuilder book=new StringBuilder("<workbook xmlns=\""+NS+"\" xmlns:r=\""+REL+"\"><sheets>");
   StringBuilder rels=new StringBuilder("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
   Set<String> used=new HashSet<>();int n=0;for(Map.Entry<String,List<Person>> group:groups.entrySet()){
    n++;String name=sheetName(group.getKey(),used);
    types.append("<Override PartName=\"/xl/worksheets/sheet").append(n).append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
    book.append("<sheet name=\"").append(escape(name).replace("\"","&quot;")).append("\" sheetId=\"").append(n).append("\" r:id=\"rId").append(n).append("\"/>");
    rels.append("<Relationship Id=\"rId").append(n).append("\" Type=\"").append(REL).append("/worksheet\" Target=\"worksheets/sheet").append(n).append(".xml\"/>");
    entry(z,"xl/worksheets/sheet"+n+".xml",sheet(group.getValue(),year,jubilees));
   }
   entry(z,"[Content_Types].xml",types.append("</Types>").toString());
   entry(z,"_rels/.rels","<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\""+REL+"/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");
   entry(z,"xl/workbook.xml",book.append("</sheets></workbook>").toString());
   entry(z,"xl/_rels/workbook.xml.rels",rels.append("<Relationship Id=\"rIdStyles\" Type=\"").append(REL).append("/styles\" Target=\"styles.xml\"/></Relationships>").toString());
   entry(z,"xl/styles.xml","<styleSheet xmlns=\""+NS+"\"><fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font><font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font></fonts><fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill><fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF235D45\"/><bgColor indexed=\"64\"/></patternFill></fill></fills><borders count=\"2\"><border><left/><right/><top/><bottom/><diagonal/></border><border><left style=\"thin\"><color rgb=\"FF000000\"/></left><right style=\"thin\"><color rgb=\"FF000000\"/></right><top style=\"thin\"><color rgb=\"FF000000\"/></top><bottom style=\"thin\"><color rgb=\"FF000000\"/></bottom><diagonal/></border></borders><cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs><cellXfs count=\"3\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/><xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment wrapText=\"1\" vertical=\"bottom\"/></xf><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\"/></cellXfs><cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles></styleSheet>");
  }return bytes.toByteArray();
 }
 private static String sheetName(String raw,Set<String> used){String base=raw.replaceAll("[\\\\/:?*\\[\\]]"," ").trim().replaceAll("^'+|'+$","");if(base.isEmpty())base="Іменинники";if(base.length()>31)base=base.substring(0,31);String name=base;int i=2;while(!used.add(name.toLowerCase(Locale.ROOT))){String suffix=" "+i++;name=base.substring(0,Math.min(base.length(),31-suffix.length()))+suffix;}return name;}
 private static String sheet(List<Person> list,int year,boolean jubilees){
  String[] headers={"Прізвище","Ім’я","По батькові","Посада","Підрозділ","Дата народження",jubilees?"Дата ювілею":"Дата іменин","Вік у "+year+" році"};
  List<String[]> rows=new ArrayList<>();rows.add(headers);for(Person p:list)rows.add(new String[]{p.last,p.first,p.middle,p.job,p.unit,p.born.format(DATE),p.birthday(year).format(DATE),Integer.toString(p.ageIn(year))});
  double[] widths=new double[headers.length];for(int ri=0;ri<rows.size();ri++){String[] a=rows.get(ri);for(int i=0;i<a.length;i++){String v=a[i];if(ri==0&&i>=5)v=i==5?"Дата\nнародження":i==6?(jubilees?"Дата\nювілею":"Дата\nіменин"):"Вік у\n"+year;widths[i]=Math.max(widths[i],Workbook.exportWidth(v,ri==0));}} headers[5]="Дата\nнародження";headers[6]=jubilees?"Дата\nювілею":"Дата\nіменин";headers[7]="Вік у\n"+year;
  StringBuilder s=new StringBuilder("<worksheet xmlns=\""+NS+"\"><dimension ref=\"A1:H"+rows.size()+"\"/><sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews><cols>");
  for(int i=0;i<widths.length;i++)s.append("<col min=\"").append(i+1).append("\" max=\"").append(i+1).append("\" width=\"").append(widths[i]).append("\" customWidth=\"1\"/>");
  s.append("</cols><sheetData>");int r=1;for(String[] a:rows){s.append("<row r=\"").append(r).append(r==1?"\" ht=\"30\" customHeight=\"1\">":"\">");for(int i=0;i<a.length;i++)cell(s,i,r,a[i],r>1&&i==7);s.append("</row>");r++;}s.append("</sheetData></worksheet>");
  return s.toString();
 }
}
