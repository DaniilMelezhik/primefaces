/*
 * Copyright 2009-2011 Prime Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.component.orderlist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import org.primefaces.component.column.Column;
import org.primefaces.renderkit.CoreRenderer;

public class OrderListRenderer extends CoreRenderer {
    
    @Override
	public void decode(FacesContext context, UIComponent component) {
		OrderList pickList = (OrderList) component;
		Map<String,String> params = context.getExternalContext().getRequestParameterMap();
        String values = pickList.getClientId(context) + "_values";
		
		if(values != null) {
			pickList.setSubmittedValue(params.get(values));
		}
	}
    
    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        OrderList ol = (OrderList) component;
        
        encodeMarkup(context, ol);
        encodeScript(context, ol);
    }

    protected void encodeMarkup(FacesContext context, OrderList ol) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
		String clientId = ol.getClientId(context);
        String controlsLocation = ol.getControlsLocation();
        String style = ol.getStyle();
        String styleClass = ol.getStyleClass();
        styleClass = styleClass == null ? OrderList.CONTAINER_CLASS : OrderList.CONTAINER_CLASS + " " + styleClass;
        
        writer.startElement("table", ol);
        writer.writeAttribute("id", clientId, null);
        writer.writeAttribute("class", styleClass, null);
        if(style != null) {
            writer.writeAttribute("style", style, null);
        }
        
        writer.startElement("tbody", null);
		writer.startElement("tr", null);
        
        if(controlsLocation.equals("left")) {
            encodeControls(context, ol);
        }
                
        encodeList(context, ol);
        
        if(controlsLocation.equals("right")) {
            encodeControls(context, ol);
        }

        writer.endElement("tr");
        writer.endElement("tbody");
        writer.endElement("table");
    }
    
    protected void encodeList(FacesContext context, OrderList ol) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = ol.getClientId(context);
        UIComponent caption = ol.getFacet("caption");
        String listStyleClass = OrderList.LIST_CLASS;

        writer.startElement("td", null);
        
        if(caption != null) {
            encodeCaption(context, caption);
            listStyleClass += " ui-corner-bottom";
        } else {
            listStyleClass += " ui-corner-all";
        }

        writer.startElement("ul", null);
        writer.writeAttribute("class", listStyleClass, null);

        String values = encodeOptions(context, ol, (List) ol.getValue());

        writer.endElement("ul");

        encodeListStateHolder(context, clientId + "_values", values);

        writer.endElement("td");
    }
    
    protected void encodeControls(FacesContext context, OrderList ol) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        
        writer.startElement("td", null);
        writer.writeAttribute("class", OrderList.CONTROLS_CLASS, null);
        encodeButton(context, ol.getMoveUpLabel(), OrderList.MOVE_UP_BUTTON_CLASS);
        encodeButton(context, ol.getMoveTopLabel(), OrderList.MOVE_TOP_BUTTON_CLASS);
        encodeButton(context, ol.getMoveDownLabel(), OrderList.MOVE_DOWN_BUTTON_CLASS);
        encodeButton(context, ol.getMoveBottomLabel(), OrderList.MOVE_BOTTOM_BUTTON_CLASS);
        writer.endElement("td");
    }
    
    @SuppressWarnings("unchecked")
	protected String encodeOptions(FacesContext context, OrderList old, List model) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		String var = old.getVar();
		Converter converter = old.getConverter();
		StringBuilder builder = new StringBuilder();
        
        for(Iterator it = model.iterator(); it.hasNext();) {
            Object item = it.next();
			context.getExternalContext().getRequestMap().put(var, item);
			String value = converter != null ? converter.getAsString(context, old, old.getItemValue()) : old.getItemValue().toString();
			
			writer.startElement("li", null);
            writer.writeAttribute("class", OrderList.ITEM_CLASS, null);
            writer.writeAttribute("data-item-value", value, null);
			
            if(old.getChildCount() > 0) {
                                
                writer.startElement("table", null);
                writer.startElement("tbody", null);
                writer.startElement("tr", null);
                        
                 for(UIComponent kid : old.getChildren()) {
                     if(kid instanceof Column && kid.isRendered()) {
                         Column column = (Column) kid;
                         
                         writer.startElement("td", null);
                         if(column.getStyle() != null) writer.writeAttribute("style", column.getStyle(), null);
                         if(column.getStyleClass() != null) writer.writeAttribute("class", column.getStyleClass(), null);
                         
                         kid.encodeAll(context);
                         writer.endElement("td");
                     }
                 }
                 
                writer.endElement("tr");
                writer.endElement("tbody");
                writer.endElement("table");
            }
            else {
                writer.writeText(old.getItemLabel(), null);
            }
                
			writer.endElement("li");
			
			builder.append("\"").append(value).append("\"");
            
            if(it.hasNext()) {
                builder.append(",");
            }
		}
		
		context.getExternalContext().getRequestMap().remove(var);
		
		return builder.toString();
	}
    
    protected void encodeButton(FacesContext context, String label, String styleClass) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
        
        writer.startElement("button", null);
        writer.writeAttribute("type", "button", null);
		writer.writeAttribute("class", styleClass, null);
        writer.write(label);
        writer.endElement("button");
	}
    
    protected void encodeListStateHolder(FacesContext context, String clientId, String values) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		
		writer.startElement("input", null);
		writer.writeAttribute("type", "hidden", null);
		writer.writeAttribute("id", clientId, null);
		writer.writeAttribute("name", clientId, null);
		writer.writeAttribute("value", values, null);
		writer.endElement("input");
	}

    protected void encodeScript(FacesContext context, OrderList ol) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
		String clientId = ol.getClientId(context);
		
        startScript(writer, clientId);

        writer.write("PrimeFaces.cw('OrderList','" + ol.resolveWidgetVar() + "',{");
        writer.write("id:'" + clientId + "'");    
        
        if(ol.isIconOnly()) writer.write("iconOnly:true");
        if(ol.getEffect() != null) writer.write(",effect:'" + ol.getEffect() + "'");
        
        writer.write("});");
		
		endScript(writer);
    }
    
    @Override
	@SuppressWarnings("unchecked")
	public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
        try {
            OrderList ol = (OrderList) component;
            List orderedList = new ArrayList();
            Converter converter = ol.getConverter();
            String[] values = ((String) submittedValue).split(",");

            for(String item : values) {
                if(isValueBlank(item))
                    continue;

                //trim whitespaces and double quotes
                String val = item.trim();
                val = val.substring(1, val.length() - 1);
            
                Object convertedValue = converter != null ? converter.getAsObject(context, ol, val) : val;

                if(convertedValue != null)
                    orderedList.add(convertedValue);
            }

            return orderedList;
        }
        catch(Exception exception) {
            throw new ConverterException(exception);
        }
	}
    
    protected void encodeCaption(FacesContext context, UIComponent caption) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement("div", null);
        writer.writeAttribute("class", OrderList.CAPTION_CLASS, null);
        caption.encodeAll(context);
        writer.endElement("div");
    }
    
    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        //Rendering happens on encodeEnd
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
