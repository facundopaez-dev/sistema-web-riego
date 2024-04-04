# Sistema Web para el Cálculo del Agua de Riego
### Problemática a resolver
En el campo, la utilización del agua de riego es económicamente costosa y de muy poco acceso, con lo cual ante la necesidad de realizar el riego de los cultivos en las parcelas se necesita optimizar al máximo este recurso.

### Alcance
El proyecto a desarrollar consistirá en un sistema que le permitirá saber al usuario la cantidad de agua de riego de un cultivo en milímetros por día, la cual será determinada en función de factores climáticos y terrestres. Los factores climáticos serán obtenidos de los informes climáticos publicados por un servicio meteorológico, mientras que los terrestres serán provistos por el usuario. Los primeros son la temperatura máxima, la temperatura mínima, la precipitación y la radiación solar, entre otros, mientras que los segundos son el cultivo, la ubicación geográfica y el riego de un cultivo. El usuario por su parte deberá registrar en el sistema la cantidad de agua de riego en milímetros por día que utilizó para satisfacer la cantidad de agua riego de un cultivo.

La cantidad de agua para riego de un cultivo se determina a partir de la lámina de agua. La lámina se define como el espesor de la capa de agua con que queda cubierta una superficie luego de una lluvia o riego. En la agricultura la lámina se refiere al espesor de agua en una hectárea y está dada en milímetros. Por ejemplo, una lámina de 1 mm se da de la relación entre 10.000 litros de agua de lluvia o de riego que caen en 1 ha (10.000 m²). Por otro lado, la necesidad de agua de un cultivo se estima a partir de la evapotranspiración potencial (ETo) y un coeficiente dado, específico para cada cultivo y etapa de crecimiento (Kc). La evapotranspiración potencial se estima a partir de parámetros climatológicos con la ecuación de Hargreaves mientras que los valores de Kc están dados por tablas. Toda la información para realizar las ecuaciones, así como los coeficientes específicos para cada uno de los cultivos y sus diferentes etapas de crecimiento está desarrollada y puesta a disposición en los manuales de la Organización de las Naciones Unidas para la Alimentación y la Agricultura (manual N°56), abreviada como FAO (_Food and Agriculture Organization_, en inglés). Finalmente, la lámina de riego para cada cultivo se calcula a partir de la evapotranspiración del cultivo (ETc) que surge del producto entre la ETo y el Kc antes descritos.

El sistema proveerá al usuario la creación, la modificación y la eliminación (lógica) de parcelas, la creación, la modificación y la eliminación de registros de plantación, la creación, modificación y eliminación de registros de riego, y la creación, modificación y eliminación de registros climáticos, la generación y eliminación de informes estadísticos de parcelas, y la visualización de los cultivos registrados. En la creación y modificación de una parcela se brindará un mapa para que el usuario seleccione la ubicación geográfica de una parcela. La ubicación geográfica de una parcela es necesaria para obtener los parámetros meteorológicos a los cuales se encuentra sometida, los cuales son necesarios para calcular la cantidad de agua de riego de un cultivo sembrado en una parcela.

Un informe estadístico de una parcela será generado mediante el uso de registros de plantación y registros de riego comprendidos en un período definido por dos fechas elegidas por el usuario. Los datos que contendrá un informe estadístico de una parcela son: el cultivo más plantado, el cultivo menos plantado, el cultivo plantado con el mayor ciclo de vida, el cultivo plantado con el menor ciclo de vida, la cantidad de días en los que una parcela no tuvo ningún cultivo plantado y la cantidad total de agua de lluvia en milímetros por período que cayó sobre una parcela.

El sistema tendrá inicio de sesión y registro de usuario, y podrá ser utilizado como administrador. El administrador podrá crear, modificar y eliminar (lógicamente) cultivos, regiones, tipos de cultivo y suelos.

### Características funcionales del sistema
- Cálculo de la cantidad o necesidad de agua de riego [mm/día] de un cultivo en la fecha actual (es decir, hoy).
- Creación, modificación y eliminación (lógica) de parcelas.
- Creación, modificación y eliminación de registros de plantación.
- Creación, modificación y eliminación de registros de riego.
- Creación, modificación y eliminación de registros climáticos.
- Creación, modificación y eliminación (lógica) de cultivos.
- Creación, modificación y eliminación (lógica) de regiones.
- Creación, modificación y eliminación (lógica) de tipos de cultivo.
- Creación, modificación y eliminación (lógica) de suelos.
- Generación y eliminación de informes estadísticos de parcelas.
- Registro (sign up) e ingreso (log in) de usuario.
- Ingreso (log in) como administrador.