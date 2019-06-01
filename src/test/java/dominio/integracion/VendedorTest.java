package dominio.integracion;

import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dominio.Vendedor;
import dominio.Producto;
import dominio.excepcion.GarantiaExtendidaException;
import dominio.repositorio.RepositorioProducto;
import dominio.repositorio.RepositorioGarantiaExtendida;
import persistencia.sistema.SistemaDePersistencia;
import testdatabuilder.ProductoTestDataBuilder;

public class VendedorTest {

	private static final String COMPUTADOR_LENOVO = "Computador Lenovo";

	private static final String STRING_DATE = "31/05/2019";
	
	private static final String FECHA_GARANTIA_ESPERADA = "20/01/2020";
	
	private static final String NOMBRE_DEL_CLIENTE = "Gustavo Gelis";
	
	private SistemaDePersistencia sistemaPersistencia;
	
	private RepositorioProducto repositorioProducto;
	private RepositorioGarantiaExtendida repositorioGarantia;

	@Before
	public void setUp() {
		
		sistemaPersistencia = new SistemaDePersistencia();
		
		repositorioProducto = sistemaPersistencia.obtenerRepositorioProductos();
		repositorioGarantia = sistemaPersistencia.obtenerRepositorioGarantia();
		
		sistemaPersistencia.iniciar();
	}
	

	@After
	public void tearDown() {
		sistemaPersistencia.terminar();
	}

	@Test
	public void generarGarantiaTest() throws ParseException {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).build();
				
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateFormat.parse(STRING_DATE));
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateFormat.parse(FECHA_GARANTIA_ESPERADA));
		
		
		// act
		vendedor.generarGarantia(producto.getCodigo(), NOMBRE_DEL_CLIENTE, calendar.getTime());
		
		// assert
		Assert.assertTrue(vendedor.tieneGarantia(producto.getCodigo()));
		Assert.assertNotNull(repositorioGarantia.obtenerProductoConGarantiaPorCodigo(producto.getCodigo()));
		
		
		Assert.assertEquals(cal.getTime(), vendedor.obtenerFechaFinGarantia(200, true, calendar.getTime()));
		
		Assert.assertEquals(producto.getCodigo(), repositorioGarantia.obtenerProductoConGarantiaPorCodigo(producto.getCodigo()).getCodigo());
	}

	@Test
	public void productoYaTieneGarantiaTest() throws ParseException {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).build();
		
		repositorioProducto.agregar(producto);
		
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		String stringDate = "31/05/2019";
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateFormat.parse(stringDate));
		
		vendedor.generarGarantia(producto.getCodigo(), NOMBRE_DEL_CLIENTE, calendar.getTime());
		try {
			
			vendedor.generarGarantia(producto.getCodigo(), NOMBRE_DEL_CLIENTE, calendar.getTime());
			fail();
			
		} catch (GarantiaExtendidaException e) {
			// assert
			Assert.assertEquals(Vendedor.EL_PRODUCTO_TIENE_GARANTIA, e.getMessage());
		}
	}
}
