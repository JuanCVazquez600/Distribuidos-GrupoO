import React, { useState } from "react";






const ExcelReportDownloader = ({ currentUser }) => {


  const [isDownloading, setIsDownloading] = useState(false);


  const [downloadStatus, setDownloadStatus] = useState(null);





  const downloadExcelReport = async () => {


    setIsDownloading(true);


    setDownloadStatus(null);





    try {


      // Call the donation-excel-service microservice


      const response = await fetch("http://localhost:8082/api/donaciones/reporte/excel", {


        method: "GET",


        headers: {


          "Content-Type": "application/json",


        },


      });





      if (!response.ok) {


        throw new Error(`Error del servidor: ${response.status} - ${response.statusText}`);


      }





      // Get the filename from the Content-Disposition header if available


      const contentDisposition = response.headers.get("Content-Disposition");


      let filename = "reporte_donaciones.xlsx";


      


      if (contentDisposition) {


        const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);


        if (filenameMatch && filenameMatch[1]) {


          filename = filenameMatch[1].replace(/['"]/g, "");


        }


      }





      // Convert response to blob


      const blob = await response.blob();


      


      // Create download link


      const downloadUrl = window.URL.createObjectURL(blob);


      const link = document.createElement("a");


      link.href = downloadUrl;


      link.download = filename;


      


      // Trigger download


      document.body.appendChild(link);


      link.click();


      document.body.removeChild(link);


      


      // Clean up


      window.URL.revokeObjectURL(downloadUrl);


      


      setDownloadStatus({


        type: "success",


        message: "📊 Reporte Excel descargado exitosamente"


      });





    } catch (error) {


      console.error("Error downloading Excel report:", error);


      setDownloadStatus({


        type: "error",


        message: `❌ Error al descargar el reporte: ${error.message}`


      });


    } finally {


      setIsDownloading(false);


      


      // Clear status message after 5 seconds


      setTimeout(() => {


        setDownloadStatus(null);


      }, 5000);


    }


  };





  return (


    <div style={{ 


      marginBottom: "var(--spacing-lg)",


      padding: "var(--spacing-lg)",


      border: "1px solid var(--border-color)",


      borderRadius: "var(--border-radius-lg)",


      background: "linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%)"


    }}>


      <div style={{


        display: "flex",


        justifyContent: "space-between",


        alignItems: "center",


        marginBottom: "var(--spacing-md)",


        flexWrap: "wrap",


        gap: "var(--spacing-md)"


      }}>


        <div>


          <h3 style={{ 


            margin: 0, 


            color: "var(--text-primary)",


            display: "flex",


            alignItems: "center",


            gap: "var(--spacing-sm)"


          }}>


            📊 Reporte Excel de Donaciones


          </h3>


          <p style={{ 


            margin: "var(--spacing-xs) 0 0 0", 


            color: "var(--text-secondary)",


            fontSize: "0.9rem"


          }}>


            Descarga un reporte completo de todas las donaciones organizadas por categoría


          </p>


        </div>


        


        <button


          className="btn btn-primary"


          onClick={downloadExcelReport}


          disabled={isDownloading}


          style={{


            display: "flex",


            alignItems: "center",


            gap: "var(--spacing-sm)",


            padding: "var(--spacing-md) var(--spacing-lg)",


            fontSize: "1rem",


            fontWeight: "600",


            minWidth: "200px",


            justifyContent: "center",


            background: isDownloading 


              ? "linear-gradient(135deg, #6c757d 0%, #5a6268 100%)"


              : "linear-gradient(135deg, var(--success-color) 0%, var(--success-hover) 100%)",


            border: "none",


            boxShadow: "var(--shadow-md)",


            transition: "all 0.3s ease"


          }}


        >


          {isDownloading ? (


            <>


              <div 


                className="spinner"


                style={{


                  width: "1.2rem",


                  height: "1.2rem",


                  borderWidth: "2px"


                }}


              ></div>


              Generando...


            </>


          ) : (


            <>


              📥 Descargar Excel


            </>


          )}


        </button>


      </div>





      {/* Status Messages */}


      {downloadStatus && (


        <div 


          style={{


            padding: "var(--spacing-md)",


            borderRadius: "var(--border-radius-md)",


            background: downloadStatus.type === "success" 


              ? "linear-gradient(135deg, var(--success-color) 0%, var(--success-hover) 100%)"


              : "linear-gradient(135deg, var(--danger-color) 0%, var(--danger-hover) 100%)",


            color: "white",


            display: "flex",


            alignItems: "center",


            gap: "var(--spacing-sm)",


            fontSize: "0.95rem",


            fontWeight: "500",


            boxShadow: "var(--shadow-sm)",


            animation: "slideIn 0.3s ease"


          }}


        >


          {downloadStatus.message}


        </div>


      )}





      {/* Information Section */}


      <div style={{


        marginTop: "var(--spacing-lg)",


        padding: "var(--spacing-md)",


        background: "rgba(13, 110, 253, 0.1)",


        borderRadius: "var(--border-radius-md)",


        border: "1px solid rgba(13, 110, 253, 0.2)"


      }}>


        <h4 style={{ 


          margin: "0 0 var(--spacing-sm) 0", 


          color: "var(--primary-color)",


          fontSize: "0.95rem",


          fontWeight: "600"


        }}>


          ℹ️ Información del Reporte


        </h4>


        <ul style={{ 


          margin: 0, 


          paddingLeft: "var(--spacing-lg)",


          color: "var(--text-secondary)",


          fontSize: "0.9rem",


          lineHeight: "1.6"


        }}>


          <li>El reporte incluye todas las donaciones registradas en el inventario</li>


          <li>Las donaciones están organizadas por categoría en hojas separadas</li>


          <li>Incluye información detallada: fecha, descripción, cantidad y usuarios</li>


          <li>Formato: Excel (.xlsx) compatible con Microsoft Excel y LibreOffice Calc</li>


        </ul>


      </div>


    </div>


  );


};





export default ExcelReportDownloader;