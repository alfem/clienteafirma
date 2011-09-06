
package es.gob.afirma.envelopers.cms;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.ietf.jgss.Oid;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.AOInvalidFormatException;
import es.gob.afirma.core.ciphers.AOCipherConfig;
import es.gob.afirma.core.ciphers.CipherConstants.AOCipherAlgorithm;
import es.gob.afirma.core.envelopers.AOEnveloper;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.signers.cms.AOCMSSigner;
import es.gob.afirma.signers.pkcs7.P7ContentSignerParameters;


/** Funcionalidad de sobres digitales con CAdES. */
public class AOCMSMultiEnveloper {
    

    private String dataTypeOID = null;
    private final Map<String, byte[]> atrib = new HashMap<String, byte[]>();
    private final Map<String, byte[]> uatrib = new HashMap<String, byte[]>();

  //TODO
    /** M&eacute;todo que realiza el resto de firmas permitidas por CADES. Son
     * las siguientes: <br/>
     * <ul>
     * <li>Data</li>
     * <li>Signed Data</li>
     * <li>Digested Data</li>
     * <li>Enveloped Data</li>
     * <li>Signed and Enveloped Data</li>
     * </ul>
     * Para la generaci&oacute;n de la clave interna se utiliza por defecto el
     * AES.
     * En el caso de que sea tipo "Enveloped data" o
     * "Signed and enveloped data", la clave se generar&aacute; usando el
     * algoritmo pasado como par&aacute;metro. Dicha clave se cifrar&aacute;
     * despu&eacute;s con la clave p&uacute;blica del certificado que identifica
     * al usuario destinatario.
     * Nota: El par&aacute;metro algorithm no es el agoritmo de cifrado, es para
     * el digestAlgorithm usado en los "Unsigned Attributes".
     * @param file
     *        Flujo de lectura de los datos a firmar.
     * @param digestAlgorithm
     *        Algoritmo a usar para la firma (SHA1withRSA, MD5withRSA,...)
     * @param type
     *        Tipo de "envelop" que se quiere hacer.
     * @param keyEntry
     *        Clave privada a usar para firmar.
     * @param certDest
     *        Certificados de los usuarios a los que va destinado el sobre
     *        digital.
     * @param cipherAlgorithm 
     *        Algoritmo utilizado para cifrar
     * @param extraParams
     *        Par&aacute;metros adicionales
     * @return Envoltorio CADES.
     * @throws AOException
     *         Cuando ocurre cualquier problema en el proceso. */
    public byte[] coEnvelop(final byte[] cmsData,
                          final String digestAlgorithm,
                          String type,
                          final PrivateKeyEntry keyEntry,
                          final X509Certificate[] certDest,
                          final AOCipherAlgorithm cipherAlgorithm,
                          String dataType,
                          Properties extraParams) throws AOException {

        

        return null;
    }
    
  public byte[] cosign(final byte[] data, final byte[] sign, String algorithm, final PrivateKeyEntry keyEntry, Properties extraParams) throws AOException {

      if (algorithm.equalsIgnoreCase("RSA")) {
          algorithm = AOSignConstants.SIGN_ALGORITHM_SHA1WITHRSA;
      }
      else if (algorithm.equalsIgnoreCase("DSA")) {
          algorithm = AOSignConstants.SIGN_ALGORITHM_SHA1WITHDSA;
      }
      
        if (extraParams == null) {
            extraParams = new Properties();
        }

        final String precalculatedDigest = extraParams.getProperty("precalculatedHashAlgorithm");

        byte[] messageDigest = null;
        if (precalculatedDigest != null) {
            messageDigest = data;
        }

        X509Certificate[] xCerts = new X509Certificate[0];
        final Certificate[] certs = keyEntry.getCertificateChain();
        if (certs != null && (certs instanceof X509Certificate[])) {
            xCerts = (X509Certificate[]) certs;
        }
        else {
            final Certificate cert = keyEntry.getCertificate();
            if (cert instanceof X509Certificate) {
                xCerts = new X509Certificate[] {
                                                (X509Certificate) cert
                };
            }
        }

        final P7ContentSignerParameters csp = new P7ContentSignerParameters(data, algorithm, xCerts);

        // tipos de datos a firmar.
        if (this.dataTypeOID == null) {
            try {
                this.dataTypeOID = PKCSObjectIdentifiers.data.getId();
            }
            catch (final Exception ex) {
                Logger.getLogger("es.gob.afirma").severe("Error al asignar el OID por defecto: " + ex);
            }
        }

        final String mode = extraParams.getProperty("mode", AOSignConstants.DEFAULT_SIGN_MODE);

        final boolean omitContent = mode.equals(AOSignConstants.SIGN_MODE_EXPLICIT) || precalculatedDigest != null;

        // Si la firma que nos introducen es SignedAndEnvelopedData
        try {
            // El parametro omitContent no tiene sentido en un signed and
            // envelopedData.
            return new CoSignerEnveloped().coSigner(csp, sign, dataTypeOID, keyEntry, atrib, uatrib, messageDigest);
        }
        catch (final Exception e) {
            throw new AOException("Error generando la Cofirma PKCS#7", e);
        }
    }

    public byte[] cosign(final byte[] sign, String algorithm, final PrivateKeyEntry keyEntry, final Properties extraParams) throws AOException {

        if (algorithm.equalsIgnoreCase("RSA")) {
            algorithm = AOSignConstants.SIGN_ALGORITHM_SHA1WITHRSA;
        }
        else if (algorithm.equalsIgnoreCase("DSA")) {
            algorithm = AOSignConstants.SIGN_ALGORITHM_SHA1WITHDSA;
        }

        // tipos de datos a firmar.
        if (this.dataTypeOID == null) {
            try {
                this.dataTypeOID = PKCSObjectIdentifiers.data.getId();
            }
            catch (final Exception ex) {
                Logger.getLogger("es.gob.afirma").severe("Error al asignar el OID por defecto: " + ex);
            }
        }

        // Algoritmo de firma.
        final String typeAlgorithm = algorithm;

        // Array de certificados
        X509Certificate[] aCertificados = new X509Certificate[0];
        final Certificate[] certs = keyEntry.getCertificateChain();
        if (certs != null && (certs instanceof X509Certificate[])) {
            aCertificados = (X509Certificate[]) certs;
        }
        else {
            final Certificate cert = keyEntry.getCertificate();
            if (cert instanceof X509Certificate) {
                aCertificados = new X509Certificate[] {
                                                       (X509Certificate) cert
                };
            }
        }

        // Cofirma de la firma usando unicamente el fichero de firmas.
        try {
            return new CoSignerEnveloped().coSigner(typeAlgorithm, aCertificados, sign, dataTypeOID, keyEntry, atrib, uatrib, null);
        }
        catch (final Exception e) {
            throw new AOException("Error generando la Cofirma PKCS#7", e);
        }
    }

    /** Tipo de los datos contenidos en la envoltura. Siempre data por
     * est&aacute;ndar. */
    private static Oid DATA_TYPE_OID;

    /** Algoritmo de firma. */
    private String signatureAlgorithm = AOSignConstants.DEFAULT_SIGN_ALGO;

    /** Atributos firmados que se desean agregar a los envoltorios firmados. */
    private final Map<Oid, byte[]> attrib = new HashMap<Oid, byte[]>();

    /** Atributos que no requieren firma y se desean agregar a todos los
     * envoltorios que los soporten. */
    private final Map<Oid, byte[]> uattrib = new HashMap<Oid, byte[]>();

    /** Clave privada del usuario que genera o abre el envoltorio. */
    private PrivateKeyEntry configuredKe = null;

    /** Clave para el descifrado de los datos de un envoltorio EncryptedData. Si
     * se utiliza un algoritmo PBE de cifrado, ser&aacute; una contrase&ntilda;a
     * en texto plano. Si es otro algoritmo ser&aacute; su clave en base 64. */
    private String cipherKey = null;

    static {
        try {
            DATA_TYPE_OID = new Oid( PKCSObjectIdentifiers.data.getId() );
        }
        catch (final Exception e) {
            /* Esto nunca podria fallar */
        }
    }

    /** Configura un atributo firmado para agregarlo a un envoltorio.
     * @param oid
     *        Object Identifier. Identificador del objeto a introducir.
     * @param value
     *        Valor asignado */
    void addSignedAttribute(final org.ietf.jgss.Oid oid, final byte[] value) {
        attrib.put(oid, value);
    }

    /** Configura un atributo no firmado para agregarlo a un envoltorio.
     * @param oid
     *        Object Identifier. Identificador del atributo a introducir.
     * @param value
     *        Valor asignado */
    void addUnsignedAttribute(final org.ietf.jgss.Oid oid, final byte[] value) {
        uattrib.put(oid, value);
    }

    /** Crea un envoltorio CMS de tipo Data.
     * @param content
     *        Datos que se desean envolver.
     * @return Envoltorio Data. */
    byte[] createCMSData(final byte[] content) {
        return new CMSData().genData(content);
    }

    /** Crea un envoltorio CMS de tipo DigestedData.
     * @param content
     *        Datos que se desean envolver.
     * @return Envoltorio DigestedData.
     * @throws IOException
     *         Error en la lectura de datos.
     * @throws NoSuchAlgorithmException
     *         Cuando el algoritmo de cifrado indicado no est&aacute;
     *         soportado. */
    byte[] createCMSDigestedData(final byte[] content) throws IOException, NoSuchAlgorithmException {
        return new CMSDigestedData().genDigestedData(content, this.signatureAlgorithm, DATA_TYPE_OID);
    }

    /** Crea un envoltorio CMS de tipo CompressedData.
     * @param content
     *        Datos que se desean envolver.
     * @return Envoltorio Compressed Data. */
    byte[] createCMSCompressedData(final byte[] content) {
        return new CMSCompressedData().genCompressedData(content);
    }

    /** Crea un envoltorio CMS de tipo EncryptedData.
     * @param content
     *        Contenido a envolver
     * @param cipherConfig
     *        Configuraci&oacute;n del cifrado del envoltorio
     * @param key
     *        Clave de envoltura
     * @return Envoltorio EncryptedData.
     * @throws NoSuchAlgorithmException
     *         Cuando el algoritmo de cifrado indicado no est&aacute;
     *         soportado.
     */
    byte[] createCMSEncryptedData(final byte[] content, final AOCipherConfig cipherConfig, final Key key) throws NoSuchAlgorithmException {
        return new CMSEncryptedData().genEncryptedData(content, signatureAlgorithm, cipherConfig, key, DATA_TYPE_OID, uattrib);
    }

    /** Crea un envoltorio CMS de tipo EnvelopedData.
     * @param content
     *        Contenido que se desea ensobrar.
     * @param ke
     *        Clave privada del remitente (s&oacute;lo si se quiere indicar
     *        remitente).
     * @param cipherConfig
     *        Configuraci&oacute;n para el cifrado de datos.
     * @param recipientsCerts
     *        Destinatarios del sobre electr&oacute;nico.
     * @return Envoltorio EnvelopedData.
     * @throws NoSuchAlgorithmException
     *         Cuando el algoritmo de cifrado indicado no est&aacute;
     *         soportado.
     * @throws IOException
     *         Error en la escritura de datos.
     * @throws CertificateEncodingException
     *         Cuando el certificado del remitente no es v&aacute;lido. */
    public byte[] createCMSEnvelopedData(final byte[] content,
                                         final PrivateKeyEntry ke,
                                         final AOCipherConfig cipherConfig,
                                         final X509Certificate[] recipientsCerts) throws NoSuchAlgorithmException,
                                                                                 CertificateEncodingException,
                                                                                 IOException {

        // Si se establecion un remitente
        if (ke != null) {
            return new CMSEnvelopedData().genEnvelopedData(this.createContentSignerParementers(content, ke, signatureAlgorithm),
                                                           cipherConfig,
                                                           recipientsCerts,
                                                           DATA_TYPE_OID,
                                                           uattrib);
        }

        // Si no se establecio remitente
        return new CMSEnvelopedData().genEnvelopedData(content, signatureAlgorithm, cipherConfig, recipientsCerts, DATA_TYPE_OID, uattrib);
    }

    /** Crea un envoltorio CMS de tipo SignedAndEnvelopedData.
     * @param content
     *        Contenido que se desea ensobrar.
     * @param ke
     *        Clave privada del remitente.
     * @param cipherConfig
     *        Configuraci&oacute;n para el cifrado de datos.
     * @param recipientsCerts
     *        Destinatarios del sobre electr&oacute;nico.
     * @return Envoltorio SignedAndEnvelopedData.
     * @throws NoSuchAlgorithmException
     *         Cuando el algoritmo de cifrado indicado no est&aacute;
     *         soportado.
     * @throws IOException
     *         Error en la escritura de datos.
     * @throws CertificateEncodingException
     *         Cuando el certificado del remitente no es v&aacute;lido.
     */
    public byte[] createCMSSignedAndEnvelopedData(final byte[] content,
                                                  final PrivateKeyEntry ke,
                                                  final AOCipherConfig cipherConfig,
                                                  final X509Certificate[] recipientsCerts) throws CertificateEncodingException,
                                                                                          NoSuchAlgorithmException,
                                                                                          IOException {
        return new CMSSignedAndEnvelopedData().genSignedAndEnvelopedData(this.createContentSignerParementers(content, ke, signatureAlgorithm),
                                                                         cipherConfig,
                                                                         recipientsCerts,
                                                                         DATA_TYPE_OID,
                                                                         ke,
                                                                         attrib,
                                                                         uattrib);
    }

    /** Crea un envoltorio CMS de tipo AuthenticatedData.
     * @param content
     *        Contenido que se desea ensobrar.
     * @param ke
     *        Clave privada del remitente.
     * @param cipherConfig
     *        Configuraci&oacute;n para el cifrado de datos.
     * @param recipientsCerts
     *        Destinatarios del sobre electr&oacute;nico.
     * @return Envoltorio AuthenticatedData.
     * @throws NoSuchAlgorithmException
     *         Cuando el algoritmo de cifrado indicado no est&aacute;
     *         soportado.
     * @throws IOException
     *         Error en la escritura de datos.
     * @throws CertificateEncodingException
     *         Cuando el certificado del remitente no es v&aacute;lido. */
    byte[] createCMSAuthenticatedData(final byte[] content, final PrivateKeyEntry ke, final AOCipherConfig cipherConfig, final X509Certificate[] recipientsCerts) throws CertificateEncodingException,
                                                                                                                                         NoSuchAlgorithmException,
                                                                                                                                         IOException {
        return new CMSAuthenticatedData().genAuthenticatedData(this.createContentSignerParementers(content, ke, signatureAlgorithm), // ContentSignerParameters
                                                               null, // Algoritmo de autenticacion (usamos el por defecto)
                                                               cipherConfig, // Configuracion del cipher
                                                               recipientsCerts, // certificados destino
                                                               DATA_TYPE_OID, // dataType
                                                               true, // applySigningTime,
                                                               attrib, // atributos firmados
                                                               uattrib // atributos no firmados
        );
    }

    /** Crea un envoltorio CMS de tipo AuthenticatedEnvelopedData.
     * @param content
     *        Contenido que se desea ensobrar.
     * @param ke
     *        Clave privada del remitente.
     * @param cipherConfig
     *        Configuraci&oacute;n para el cifrado de datos.
     * @param recipientsCerts
     *        Destinatarios del sobre electr&oacute;nico.
     * @return Envoltorio AuthenticatedEnvelopedData.
     * @throws NoSuchAlgorithmException
     *         Cuando el algoritmo de cifrado indicado no est&aacute;
     *         soportado.
     * @throws IOException
     *         Error en la escritura de datos.
     * @throws CertificateEncodingException
     *         Cuando el certificado del remitente no es v&aacute;lido. */
    public byte[] createCMSAuthenticatedEnvelopedData(final byte[] content,
                                                      final PrivateKeyEntry ke,
                                                      final AOCipherConfig cipherConfig,
                                                      final X509Certificate[] recipientsCerts) throws CertificateEncodingException,
                                                                                        NoSuchAlgorithmException,
                                                                                        IOException {
        return new CMSAuthenticatedEnvelopedData().genAuthenticatedEnvelopedData(this.createContentSignerParementers(content, ke, signatureAlgorithm), // ContentSignerParameters
                                                                                 null, // Algoritmo de autenticacion (usamos el por
                                                                                       // defecto)
                                                                                 cipherConfig, // Configuracion del cipher
                                                                                 recipientsCerts, // certificados destino
                                                                                 DATA_TYPE_OID, // dataType
                                                                                 true, // applySigningTime,
                                                                                 attrib, // atributos firmados
                                                                                 uattrib // atributos no firmados
        );
    }

    /** Genera el bloque de datos con la informaci&oacute;n del remitente de un
     * mensaje.
     * @param content
     *        Mensaje.
     * @param ke
     *        Clave privada del remitente.
     * @param digestAlgorithm
     *        Algoritmo de huella digital.
     * @return Bloque de datos con la informaci&oacute;n del remitente. */
    private P7ContentSignerParameters createContentSignerParementers(final byte[] content, final PrivateKeyEntry ke, final String digestAlgorithm) {
        X509Certificate[] xCerts = new X509Certificate[0];
        final Certificate[] certs = ke.getCertificateChain();
        if (certs != null && (certs instanceof X509Certificate[])) {
            xCerts = (X509Certificate[]) certs;
        }
        else {
            final Certificate cert = ke.getCertificate();
            if (cert instanceof X509Certificate) {
                xCerts = new X509Certificate[] {
                    (X509Certificate) cert
                };
            }
        }
        return new P7ContentSignerParameters(content, digestAlgorithm, xCerts);
    }

    /** Agrega un nuevo remitente a un envoltorio CMS compatible.
     * @param envelop
     *        Envoltorio original.
     * @param ke
     *        Referencia a la clave privada del certificado del remitente.
     * @return Envoltorio con el nuevo remitente.
     * @throws AOException
     *         Cuando se produce un error al agregar el nuevo remitente.
     * @throws AOInvalidFormatException
     *         Tipo de envoltorio no soportado. */
    byte[] addOriginator(final byte[] envelop, final PrivateKeyEntry ke) throws AOException, AOInvalidFormatException {
        String contentInfo;
        final ValidateCMS validator = new ValidateCMS();
        if (validator.isCMSEnvelopedData(envelop)) {
            contentInfo = AOSignConstants.CMS_CONTENTTYPE_ENVELOPEDDATA;
        }
        else if (validator.isCMSSignedAndEnvelopedData(envelop)) {
            contentInfo = AOSignConstants.CMS_CONTENTTYPE_SIGNEDANDENVELOPEDDATA;
        }
        else if (validator.isCMSAuthenticatedEnvelopedData(envelop)) {
            contentInfo = AOSignConstants.CMS_CONTENTTYPE_AUTHENVELOPEDDATA;
        }
        else {
            throw new AOInvalidFormatException("Los datos proporcionado no son un envoltorio que soporte multiples remitentes");
        }
        return addOriginator(envelop, contentInfo, ke);
    }

    /** Agrega los datos de un remitente adicional a un envoltorio compatible.
     * Los envoltorios que admiten m&aacute;s de un remitente son:
     * <ul>
     * <li>Enveloped Data</li>
     * <li>Authenticated Data</li>
     * <li>Authenticated And Enveloped Data</li>
     * <li>Signed And Enveloped Data</li>
     * </ul>
     * @param envelop
     *        Estructura a la que se le desea agregar un remitente.
     * @param contentInfo
     *        Tipo de contenido que se desea envolver.
     * @param ke
     *        Referencia a la clave privada del certificado del remitente.
     * @throws AOException
     *         Cuando ocurrio un error al agregar el remitente a la
     *         estructura.
     * @throws IllegalArgumentException
     *         Cuando se indica un contentInfo no compatible con
     *         m&uacute;tiples remitentes. */
    private byte[] addOriginator(final byte[] envelop, final String contentInfo, final PrivateKeyEntry ke) throws AOException {

        byte[] newEnvelop;

        X509Certificate[] xCerts = new X509Certificate[0];
        final Certificate[] certs = ke.getCertificateChain();
        if (certs != null && (certs instanceof X509Certificate[])) {
            xCerts = (X509Certificate[]) certs;
        }
        else {
            final Certificate cert = ke.getCertificate();
            if (cert instanceof X509Certificate) {
                xCerts = new X509Certificate[] {
                    (X509Certificate) cert
                };
            }
        }

        final X509Certificate[] originatorCertChain = xCerts;

        if (contentInfo.equals(AOSignConstants.CMS_CONTENTTYPE_ENVELOPEDDATA)) {
            final CMSEnvelopedData enveloper = new CMSEnvelopedData();
            newEnvelop = enveloper.addOriginatorInfo(envelop, originatorCertChain);
        }
        else if (contentInfo.equals(AOSignConstants.CMS_CONTENTTYPE_SIGNEDANDENVELOPEDDATA)) {
            newEnvelop = new AOCMSSigner().cosign(envelop, AOSignConstants.DEFAULT_SIGN_ALGO, ke, null);
        }
        else if (contentInfo.equals(AOSignConstants.CMS_CONTENTTYPE_AUTHENVELOPEDDATA)) {
            final CMSAuthenticatedEnvelopedData enveloper = new CMSAuthenticatedEnvelopedData();
            newEnvelop = enveloper.addOriginatorInfo(envelop, originatorCertChain);

        }
        else {
            throw new IllegalArgumentException("La estructura para el ContentInfo indicado no esta soportada o " + "no admite multiples remitentes");
        }

        if (newEnvelop == null) {
            throw new AOException("Error al agregar el nuevo remitente al envoltorio");
        }

        return newEnvelop;
    }

    /** Algoritmo de firma que se utilizar&aacute; internamente en el sobre. El
     * algoritmo de huella digital se tomar&aacute; de este.
     * @param algorithm
     *        Algoritmo de firma. */
    public void setSignatureAlgorithm(final String algorithm) {
        this.signatureAlgorithm = (algorithm == null ? AOSignConstants.DEFAULT_SIGN_ALGO : algorithm);
    }

    /** Establece la clave privada del remitente del envoltorio.
     * @param originatorKe
     *        Clave del remitente. */
    void setOriginatorKe(final PrivateKeyEntry originatorKe) {
        this.configuredKe = originatorKe;
    }

    /** Establece la contrase&ntilde;a o clave para la encriptaci&oacute;n de los
     * datos.
     * @param keyPass
     *        Clave en base 64 o contrase&ntilda;a de cifrado. */
    void setCipherKey(final String keyPass) {
        this.cipherKey = keyPass;
    }

    /** Recupera el algoritmo de firma configurado. El algoritmo de huella
     * digital se tomar&aacute; de este.
     * @return Cadena de texto identificativa para el algoritmo de firma */
    String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    /** Recupera la clave privada del remitente del envoltorio.
     * @return Clave del remitente. */
    PrivateKeyEntry getOriginatorKe() {
        return configuredKe;
    }

    /** Recupera la clave o contrascontrase&ntilde;a para la encriptaci&oacute;n
     * de los datos.
     * @return Clave en base 64 o contrase&ntilda;a de cifrado. */
    String getCipherKey() {
        return this.cipherKey;
    }

    /** Recupera el contenido de un envoltorio CMS.
     * @param cmsEnvelop
     *        Envoltorio CMS.
     * @return Contenido del envoltorio.
     * @throws AOInvalidRecipientException
     *         Cuando el usuario no es uno de los destinatarios del sobre.
     * @throws InvalidKeyException
     *         Cuando la clave de descifrado configurada no es
     *         v&aacute;lida.
     * @throws CertificateEncodingException
     *         Cuando el certificado del destinatario no es v&aacute;lido.
     * @throws IOException
     *         Cuando el envoltorio est&aacute; corrupto o no puede leerse.
     * @throws AOInvalidFormatException
     *         Cuando no se ha indicado un envoltorio soportado.
     * @throws AOException
     *         Cuando se produce un error durante al desenvolver los datos. */
    byte[] recoverData(final byte[] cmsEnvelop) throws AOInvalidRecipientException,
                                         InvalidKeyException,
                                         CertificateEncodingException,
                                         IOException,
                                         AOInvalidFormatException,
                                         AOException {

        final org.bouncycastle.asn1.ASN1InputStream is = new org.bouncycastle.asn1.ASN1InputStream(cmsEnvelop);

        // Leemos los datos
        final org.bouncycastle.asn1.ASN1Sequence dsq;
        try {
            dsq = (org.bouncycastle.asn1.ASN1Sequence) is.readObject();
        }
        finally {
            try {
                is.close();
            }
            catch (final Exception e) {}
        }

        final Enumeration<?> objects = dsq.getObjects();

        // Elementos que contienen los elementos OID Data
        final org.bouncycastle.asn1.DERObjectIdentifier doi = (org.bouncycastle.asn1.DERObjectIdentifier) objects.nextElement();

        byte[] datos;
        if (doi.equals(org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.data)) {
            Logger.getLogger("es.gob.afirma").warning("La extraccion de datos de los envoltorios CMS Data no esta implementada"); //$NON-NLS-1$
            datos = null;
            // datos = this.recoverCMSEncryptedData(cmsEnvelop, cipherKey);
        }
        else if (doi.equals(org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.digestedData)) {
            Logger.getLogger("es.gob.afirma").warning("La extraccion de datos de los envoltorios CMS DigestedData no esta implementada");
            datos = null;
            // datos = this.recoverCMSEncryptedData(cmsEnvelop, cipherKey);
        }
        else if (doi.equals(org.bouncycastle.asn1.cms.CMSObjectIdentifiers.compressedData)) {
            datos = this.recoverCMSCompressedData(cmsEnvelop);
        }
        else if (doi.equals(org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.encryptedData)) {
            datos = this.recoverCMSEncryptedData(cmsEnvelop, cipherKey);
        }
        else if (doi.equals(org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.envelopedData)) {
            datos = this.recoverCMSEnvelopedData(cmsEnvelop, configuredKe);
        }
        else if (doi.equals(org.bouncycastle.asn1.cms.CMSObjectIdentifiers.authEnvelopedData)) {
            datos = this.recoverCMSAuthenticatedEnvelopedData(cmsEnvelop, configuredKe);
        }
        else if (doi.equals(org.bouncycastle.asn1.cms.CMSObjectIdentifiers.authenticatedData)) {
            datos = this.recoverCMSAuthenticatedData(cmsEnvelop, configuredKe);
        }
        else if (doi.equals(org.bouncycastle.asn1.cms.CMSObjectIdentifiers.signedAndEnvelopedData)) {
            datos = this.recoverCMSSignedEnvelopedData(cmsEnvelop, configuredKe);
        }
        else {
            throw new AOInvalidFormatException("Los datos introducidos no se corresponden con un tipo de objeto CMS soportado");
        }

        return datos;
    }

    /** Recupera el contenido de un envoltorio CompressedData.
     * @param compressedData
     *        Envoltorio CMS de tipo CompressedData.
     * @return Contenido del envoltorio.
     * @throws IOException
     *         Cuando ocurre un error al descomprimir los datos. */
    byte[] recoverCMSCompressedData(final byte[] compressedData) throws IOException {
        return new CMSCompressedData().getContentCompressedData(compressedData);
    }

    /** Recupera el contenido de un envoltorio EncryptedData.
     * @param encryptedData
     *        Envoltorio CMS de tipo EncryptedData.
     * @param passkey
     *        Contrase&ntilde;a o clave (base64) necesaria para desencriptar
     *        los datos.
     * @return Contenido del envoltorio.
     * @throws InvalidKeyException
     *         Cuando la clave proporcionada no es v&aacute;lida.
     * @throws AOException
     *         Cuando se produce un error al desenvolver los datos. */
    byte[] recoverCMSEncryptedData(final byte[] encryptedData, final String passkey) throws InvalidKeyException, AOException {
        return new CMSDecipherEncryptedData().dechiperEncryptedData(encryptedData, passkey);
    }

    /** Recupera el contenido de un envoltorio EnvelopedData.
     * @param envelopedData
     *        Envoltorio CMS de tipo EnvelopedData.
     * @param ke
     *        Clave de un destinatario del sobre.
     * @return Contenido del envoltorio.
     * @throws IOException
     *         Si ocurre alg&uacute;n problema leyendo o escribiendo los
     *         datos
     * @throws CertificateEncodingException
     *         Si se produce alguna excepci&oacute;n con los certificados de
     *         firma.
     * @throws AOException
     *         Cuando ocurre un error durante el proceso de descifrado
     *         (formato o clave incorrecto,...)
     * @throws AOInvalidRecipientException
     *         Cuando se indica un certificado que no est&aacute; entre los
     *         destinatarios del sobre.
     * @throws InvalidKeyException
     *         Cuando la clave almacenada en el sobre no es v&aacute;lida. */
    byte[] recoverCMSEnvelopedData(final byte[] envelopedData, final PrivateKeyEntry ke) throws IOException,
                                                                                        CertificateEncodingException,
                                                                                        AOException,
                                                                                        AOInvalidRecipientException,
                                                                                        InvalidKeyException {
        return new CMSDecipherEnvelopData().dechiperEnvelopData(envelopedData, ke);
    }

    /** Recupera el contenido de un envoltorio SignedEnvelopedData.
     * @param signedEnvelopedData
     *        Envoltorio CMS de tipo SignedEnvelopedData.
     * @param ke
     *        Clave de un destinatario del sobre.
     * @return Contenido del envoltorio.
     * @throws IOException
     *         Si ocurre alg&uacute;n problema leyendo o escribiendo los
     *         datos
     * @throws CertificateEncodingException
     *         Si se produce alguna excepci&oacute;n con los certificados de
     *         firma.
     * @throws AOException
     *         Cuando ocurre un error durante el proceso de descifrado
     *         (formato o clave incorrecto,...)
     * @throws AOInvalidRecipientException
     *         Cuando se indica un certificado que no est&aacute; entre los
     *         destinatarios del sobre.
     * @throws InvalidKeyException
     *         Cuando la clave almacenada en el sobre no es v&aacute;lida. */
    byte[] recoverCMSSignedEnvelopedData(final byte[] signedEnvelopedData, final PrivateKeyEntry ke) throws IOException,
                                                                                                    CertificateEncodingException,
                                                                                                    AOException,
                                                                                                    AOInvalidRecipientException,
                                                                                                    InvalidKeyException {
        return new CMSDecipherSignedAndEnvelopedData().dechiperSignedAndEnvelopData(signedEnvelopedData, ke);
    }

    /** Comprueba la integridad de un envoltorio AuthenticatedData y, si es
     * correcto, extrae su contenido.
     * @param authenticatedData
     *        Envoltorio CMS de tipo AuthenticatedData.
     * @param ke
     *        Clave de un destinatario del sobre.
     * @return Contenido del envoltorio.
     * @throws IOException
     *         Si ocurre alg&uacute;n problema leyendo o escribiendo los
     *         datos
     * @throws CertificateEncodingException
     *         Si el certificado proporcionado no se adec&uacute;a a la
     *         norma X.509v3
     * @throws AOException
     *         Cuando ocurre un error durante el proceso de
     *         extracci&oacute;n.
     * @throws InvalidKeyException
     *         Cuando la clave almacenada en el sobre no es v&aacute;lida. */
    byte[] recoverCMSAuthenticatedData(final byte[] authenticatedData, final PrivateKeyEntry ke) throws IOException,
                                                                                                CertificateEncodingException,
                                                                                                AOException,
                                                                                                InvalidKeyException {
        return new CMSDecipherAuthenticatedData().decipherAuthenticatedData(authenticatedData, ke);
    }

    /** Recupera el contenido de un envoltorio AuthenticatedEnvelopedData.
     * @param authenticatedEnvelopedData
     *        Envoltorio CMS de tipo AuthenticatedEnvelopedData.
     * @param ke
     *        Clave de un destinatario del sobre.
     * @return Contenido del envoltorio.
     * @throws IOException
     *         Si ocurre algun error gen&eacute;rico de entrada/salida
     * @throws IOException
     *         Si ocurre alg&uacute;n problema leyendo o escribiendo los
     *         datos
     * @throws CertificateEncodingException
     *         Si se produce alguna excepci&oacute;n con los certificados de
     *         firma.
     * @throws AOException
     *         Cuando ocurre un error durante el proceso de descifrado
     *         (formato o clave incorrecto,...)
     * @throws AOInvalidRecipientException
     *         Cuando se indica un certificado que no est&aacute; entre los
     *         destinatarios del sobre.
     * @throws InvalidKeyException
     *         Cuando la clave almacenada en el sobre no es v&aacute;lida. */
    byte[] recoverCMSAuthenticatedEnvelopedData(final byte[] authenticatedEnvelopedData, final PrivateKeyEntry ke) throws IOException,
                                                                                                                  CertificateEncodingException,
                                                                                                                  AOException,
                                                                                                                  AOInvalidRecipientException,
                                                                                                                  InvalidKeyException {
        return new CMSDecipherAuthenticatedEnvelopedData().dechiperAuthenticatedEnvelopedData(authenticatedEnvelopedData, ke);
    }

    /** M&eacute;todo que comprueba que unos datos se corresponden con una
     * estructura CMS/PKCS#7. Se realiza la verificaci&ocute;n sobre los los
     * siguientes tipos reconocidos:
     * <ul>
     * <li>Data</li>
     * <li>Signed Data</li>
     * <li>Digested Data</li>
     * <li>Encrypted Data</li>
     * <li>Enveloped Data</li>
     * <li>Signed and Enveloped Data</li>
     * <li>Authenticated Data</li>
     * <li>Authenticated and Enveloped Data</li>
     * </ul>
     * @param cmsData
     *        Datos que deseamos comprobar.
     * @return La validez del archivo cumpliendo la estructura. */
    public boolean isCMSValid(final byte[] cmsData) {
        return CMSHelper.isCMSValid(cmsData);
    }

    /** M&eacute;todo que comprueba que unos datos se corresponden con una
     * estructura CMS/PKCS#7 concreta.
     * @param data
     *        Datos que deseamos comprobar.
     * @param type
     *        Tipo de contenido del envoltorio que queremos comprobar.
     * @return Indica los datos son una envoltura CMS con el tipo de contenido
     *         indicado. */
    public boolean isCMSValid(final byte[] data, final String type) {
        return CMSHelper.isCMSValid(data, type);
    }
}
