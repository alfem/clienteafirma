package es.gob.afirma.miniapplet;

import java.awt.Component;
import java.security.KeyException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivilegedExceptionAction;
import java.util.List;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.misc.Platform.BROWSER;
import es.gob.afirma.core.misc.Platform.OS;
import es.gob.afirma.keystores.main.common.AOKeyStore;
import es.gob.afirma.keystores.main.common.AOKeyStoreManager;
import es.gob.afirma.keystores.main.common.AOKeyStoreManagerFactory;
import es.gob.afirma.keystores.main.common.AOKeystoreAlternativeException;
import es.gob.afirma.keystores.main.common.KeyStoreUtilities;
import es.gob.afirma.keystores.main.filters.CertificateFilter;

/** Acci&oacute;n privilegiada para la selecci&oacute;n de una clave privada
 * de firma por el usuario.
 * @author Carlos Gamuci Mill&aacute;n. */
final class SelectPrivateKeyAction implements PrivilegedExceptionAction<PrivateKeyEntry> {

	private final AOKeyStore keyStore;
	private final Component parent;
	private final CertFilterManager filterManager;
	
	private String library = null;
	
	   /** Crea la acci&oacute;n para la selecci&oacute;n de la clave privada de un certificado.
     * @param type Tipo de almac&eacute;n de certificados y claves privadas a usar.
     * @param lib Fichero asociado al almac&aacute;n (biblioteca din&aacute;mica en el caso de PKCS#11,
     *            fichero PFX en el caso de PKCS#12, archivo de llavero en el caso de llavero de 
     *            Mac OS X, etc.
     * @param filterManager Manejador de filtros de certificados.
     * @param parent Componente padre para los di&aacute;logos que se
     * visualizan como parte de la acci&oacute;n. */
	SelectPrivateKeyAction(final AOKeyStore type, 
	                       final String lib,
	                       final CertFilterManager filterManager, 
                           final Component parent) {
	    if (type == null) {
	        throw new IllegalArgumentException("El tipo de almacen no puede ser nulo"); //$NON-NLS-1$
	    }
	    this.keyStore = type;
	    this.filterManager = filterManager;
	    this.parent = parent;
	    this.library = lib;
	}
	
	/** Crea la acci&oacute;n para la selecci&oacute;n de la clave privada de un certificado.
	 * @param os Sistema operativo actual.
	 * @param browser Navegador web actual.
	 * @param filterManager Manejador de filtros de certificados.
	 * @param parent Componente padre para los di&aacute;logos que se
	 * visualizan como parte de la acci&oacute;n. */
	SelectPrivateKeyAction(final OS os, 
	                       final BROWSER browser, 
	                       final CertFilterManager filterManager, 
	                       final Component parent) {
        if (browser == BROWSER.FIREFOX) {
            this.keyStore = AOKeyStore.MOZ_UNI;
        } 
        else if (os == OS.WINDOWS) {
			this.keyStore = AOKeyStore.WINDOWS;
		} 
		else if (os == OS.LINUX || os == OS.SOLARIS) {
			this.keyStore = AOKeyStore.MOZ_UNI;
		} 
		else if (os == OS.MACOSX) {
			this.keyStore = AOKeyStore.APPLE;
		} 
		else {
			this.keyStore = AOKeyStore.PKCS12; 	
		}
		this.filterManager = filterManager;
		this.parent = parent;
	}
	
	public PrivateKeyEntry run() throws KeyException, AOKeystoreAlternativeException, AOException {
		final AOKeyStoreManager ksm = AOKeyStoreManagerFactory.getAOKeyStoreManager(
			this.keyStore, 
			this.library, 
			null,
			KeyStoreUtilities.getPreferredPCB(this.keyStore, this.parent), 
			this.parent
		);
		
		boolean mandatoryCertificate = false;
		List<CertificateFilter> filters = null;
		if (this.filterManager != null) {
			filters = this.filterManager.getFilters();
			mandatoryCertificate = this.filterManager.isMandatoryCertificate();
		}
		
		final String selectedAlias = KeyStoreUtilities.showCertSelectionDialog(
			ksm.getAliases(),
			ksm,
			this,
			true,
			true,
			true,
			filters,
			mandatoryCertificate
		);
    	
    	return ksm.getKeyEntry(selectedAlias,
    			KeyStoreUtilities.getCertificatePC(this.keyStore, this.parent));
	}

	
}
