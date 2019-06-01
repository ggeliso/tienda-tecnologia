package dominio;

import dominio.repositorio.RepositorioProducto;
import dominio.excepcion.GarantiaExtendidaException;
import dominio.repositorio.RepositorioGarantiaExtendida;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Vendedor {

    public static final String EL_PRODUCTO_TIENE_GARANTIA = "El producto ya cuenta con una garantia extendida";
    public static final String EL_PRODUCTO_TIENE_TRES_VOCALES = "Este producto no cuenta con garantía extendida";
    public static final String PATTERN = "[a|e|i|o|u|A|E|I|O|U]";
    public static final int TOTAL_DIAS_GARANTIA_SIN_LUNES = 200;
    public static final int TOTAL_DIAS_GARANTIA_CON_LUNES = 100;
    public static final int DIA_EXCLUIDO = Calendar.MONDAY; 
    public static final int PRECIO_PRODUCTO = 500000;

    private RepositorioProducto repositorioProducto;
    private RepositorioGarantiaExtendida repositorioGarantia;

    public Vendedor(RepositorioProducto repositorioProducto, RepositorioGarantiaExtendida repositorioGarantia) {
        this.repositorioProducto = repositorioProducto;
        this.repositorioGarantia = repositorioGarantia;

    }

    public void generarGarantia(String codigo, String nombreCliente, Date fechaSolicitudGarantia) throws ParseException {
    	
    	boolean garantia = this.tieneGarantia(codigo);
    	
    	if (garantia) {
    		throw new GarantiaExtendidaException(EL_PRODUCTO_TIENE_GARANTIA);
    	}
    	
    	Pattern pattern = Pattern.compile(PATTERN);
    	Matcher matcher = pattern.matcher(codigo);
    	int contador = 0;
    	
    	while(matcher.find()){
    		++contador;
    	}
    	
    	if (contador > 3){
    		throw new GarantiaExtendidaException(EL_PRODUCTO_TIENE_TRES_VOCALES);
    	}
        
    	GarantiaExtendida garantiaExtendida = new GarantiaExtendida(repositorioProducto.obtenerPorCodigo(codigo), nombreCliente);
    	
    	double precioProducto = repositorioProducto.obtenerPorCodigo(codigo).getPrecio();
    	double precioGarantia = 0L;
    	Date fechaGarantia = null;
    	
    	if(precioProducto > PRECIO_PRODUCTO){
    		precioGarantia = precioProducto * 0.2;
    		fechaGarantia = obtenerFechaFinGarantia(TOTAL_DIAS_GARANTIA_SIN_LUNES, true, fechaSolicitudGarantia);
    	} else {
    		precioGarantia = precioProducto * 0.1;
    		fechaGarantia = obtenerFechaFinGarantia(TOTAL_DIAS_GARANTIA_CON_LUNES, false, fechaSolicitudGarantia);
    	}
    	
    	garantiaExtendida = new GarantiaExtendida(repositorioProducto.obtenerPorCodigo(codigo), 
    			fechaSolicitudGarantia, fechaGarantia, precioGarantia, nombreCliente);
    	
    	repositorioGarantia.agregar(garantiaExtendida);    	
    	    	
    }
    
    public Date obtenerFechaFinGarantia(int totalDias, boolean excluirLunes, Date fechaSolicitud) throws ParseException{
    	
    	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    	String stringDate = dateFormat.format(fechaSolicitud);
    	Date fechaFinGarantia = dateFormat.parse(stringDate);
    	
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(fechaFinGarantia);
    	
    	for(int index = 0; index < totalDias; index ++){
    		
    		calendar.add(Calendar.DATE, 1);
    		
    		if(excluirLunes && DIA_EXCLUIDO == calendar.get(Calendar.DAY_OF_WEEK)){
    			calendar.add(Calendar.DATE, 1);
    		}
    	}
    	
    	if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            calendar.add(Calendar.DATE, 1);
        }
    	
        if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
            calendar.add(Calendar.DATE,2);
        }
    	
    	return calendar.getTime();
    }
    
    public boolean tieneGarantia(String codigo) {
        return (this.repositorioGarantia.obtenerProductoConGarantiaPorCodigo(codigo) != null ? true : false);
    }

}
