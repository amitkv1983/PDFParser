package com.bci.diablo.entity;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class Table {

    //--------------------------------------------------------------------------
    //  Members
    private final int pageIdx;
    private final List<TableRow> rows = new ArrayList<>();
    private final int columnsCount;

    //--------------------------------------------------------------------------
    //  Initialization and releasation
    public Table(int idx, int columnsCount) {
        this.pageIdx = idx;
        this.columnsCount = columnsCount;
    }

    //--------------------------------------------------------------------------
    //  Getter N Setter    
    public int getPageIdx() {
        return pageIdx;
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public String toHtml() {
        return toString(true);
    }

    public String toJson() {
        return toJson(true);
    }

    //--------------------------------------------------------------------------
    //  Method binding
    //--------------------------------------------------------------------------
    //  Implement N Override
    @Override
    public String toString() {
        return toString(false);
    }

    //--------------------------------------------------------------------------
    //  Utils
    private String toString(boolean inHtmlFormat) {
        StringBuilder retVal = new StringBuilder();
        if (inHtmlFormat) {
            retVal.append("<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset='utf-8'>")
                    .append("</head>")
                    .append("<body>");
            retVal.append("<table border='1'>");
        }
        ArrayList<SummaryRow> summaryRows= new ArrayList<SummaryRow>();
        for (TableRow row : rows) {
        	
            if (inHtmlFormat) {
                retVal.append("<tr>");
            } else if (retVal.length() > 0) {
                retVal.append("\n");
            }
            int cellIdx = 0;//pointer of row.cells
            int columnIdx = 0;//pointer of columns
            while (columnIdx < columnsCount) {
            	SummaryRow sr = new SummaryRow();
                if (cellIdx < row.getCells().size()) {
                    TableCell cell = row.getCells().get(cellIdx);
                    if (cell.getIdx() == columnIdx) {
                        if (inHtmlFormat) {
//                        	if ( cell.getContent() != "Summary Table " ){
//                        		if ( cell.getContent() == "" ){
                            retVal.append("<td>")
                                    .append(cell.getContent())
                                    .append("</td>");
//                            System.out.println(cell.getContent());
                            sr.setSeverity(cell.getContent());
//                        		}
//                        	}
                        } else {
                            if (cell.getIdx() != 0) {
                                retVal.append(";");
                            }
                            retVal.append(cell.getContent());
                        }
                        cellIdx++;
                        columnIdx++;
                    } else if (columnIdx < cellIdx) {
                        if (inHtmlFormat) {
                            retVal.append("<td>")
                                    .append("</td>");
                        } else if (columnIdx != 0) {
                            retVal.append(";");
                        }
                        columnIdx++;
                    } else {
                        throw new RuntimeException("Invalid state");
                    }
                } else {
                    if (inHtmlFormat) {
                        retVal.append("<td>")
                                .append("</td>");
                    } else if (columnIdx != 0) {
                        retVal.append(";");
                    }
                    columnIdx++;
                }

            }
            if (inHtmlFormat) {
                retVal.append("</tr>");
            }
        }
        if (inHtmlFormat) {
            retVal.append(
                    "</table>")
                    .append("</body>")
                    .append("</html>");
        }
        System.out.println(retVal.toString());
        
        
        return retVal.toString();
    }

    //--------------------------------------------------------------------------
    //  Inner class

    private String toJson(boolean inJsonFormat){
    	JSONObject Json = new JSONObject();
//    	TableRow row = rows;
    	for (TableRow row : rows){
    		if (inJsonFormat){
    			Json.put("Test", "Test");
    		}
    	
    	
       	int cellIdx = 0;//pointer of row.cells
       	int columnIdx = 0;//pointer of columns
       	while (columnIdx < columnsCount){
       		System.out.println("=============================================================================");
       		
       		System.out.println(cellIdx);
        	System.out.println(columnIdx);
        	
			if (cellIdx < row.getCells().size()) {
        		System.out.println(row.getCells().size());
        		TableCell cell = row.getCells().get(cellIdx);
        		if (cell.getIdx() == columnIdx){
        			System.out.println(cell.getIdx());
        			System.out.println(cell.getContent());
        			
        		}
        		columnIdx++;
        		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        	}
        }
    	}
       	return Json.toString();
    }
}
