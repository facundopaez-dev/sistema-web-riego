# Sistema Web para el Cálculo del Agua de Riego
### Problemática a resolver
En el campo, la utilización del agua de riego es económicamente costosa y de muy poco acceso, con lo cual ante la necesidad de realizar el riego de los cultivos en las parcelas se necesita optimizar al máximo este recurso.

### Alcance
El proyecto a desarrollar consistirá en un sistema que le permitirá saber al usuario la necesidad de agua de riego de un cultivo en milímetros por día, la cual será determinada en función de factores climáticos y terrestres. Los factores climáticos serán obtenidos de los informes climáticos publicados por un servicio meteorológico, mientras que los terrestres serán provistos por el usuario. Los primeros son la temperatura máxima, la temperatura mínima, la precipitación y la radiación solar, entre otros, mientras que los segundos son el cultivo, la ubicación geográfica y el riego de un cultivo. El usuario por su parte deberá registrar en el sistema la cantidad de agua de riego en milímetros por día que utilizó para satisfacer la necesidad de agua de riego de un cultivo.

La necesidad de agua de riego de un cultivo estará dada en milímetros por día y el sistema la calculará únicamente para un cultivo plantado bajo condiciones óptimas. Por lo tanto, el sistema servirá para realizar un riego diario sobre un cultivo plantado bajo condiciones óptimas. Por "condiciones óptimas" se hace referencia a un cultivo que se encuentra exento de enfermedades, con buena fertilización y que se desarrolla en parcelas amplias, bajo óptimas condiciones de suelo y agua, y que alcanza la máxima producción de acuerdo a las condiciones climáticas reinantes. Para determinar la necesidad de agua de riego de un cultivo en milímetros por día se necesita la evapotranspiración del cultivo de referencia (ETo) y la evapotranspiración del cultivo bajo condiciones estándar (ETc). Para calcular la ETo se utilizará la formula de Hargreaves, la cual se encuentra en la página 64 del libro _Evapotranspiración del cultivo_ de la FAO (Organización de las Naciones Unidas para la Alimentación y la Agricultura, en español). La ETo es necesaria para calcular la ETc, la cual representa la necesidad de agua de un cultivo plantado bajo condiciones óptimas.

El sistema proveerá al usuario la creación, la modificación y la eliminación (lógica) de parcelas, la creación, la modificación y la eliminación de registros de plantación, la creación y modificación de registros de riego, y la creación, modificación y eliminación de registros climáticos, la generación y eliminación de informes estadísticos de parcelas, y la visualización de los cultivos registrados. En la creación y modificación de una parcela se brindará un mapa para que el usuario seleccione la ubicación geográfica de una parcela. La ubicación geográfica de una parcela es necesaria para obtener los datos meteorológicos a los cuales se encuentra sometida, los cuales son necesarios para calcular la necesidad de agua de riego de un cultivo sembrado en una parcela.

Un informe estadístico de una parcela será generado mediante el uso de registros de plantación y registros de riego comprendidos en un período definido por dos fechas elegidas por el usuario. Los datos que contendrá un informe estadístico de una parcela son: el cultivo más plantado, el cultivo menos plantado, el cultivo plantado con el mayor ciclo de vida, el cultivo plantado con el menor ciclo de vida, la cantidad de días en los que una parcela no tuvo ningún cultivo plantado y la cantidad total de agua de lluvia en milímetros por período que cayó sobre una parcela.

El sistema tendrá inicio de sesión y registro de usuario, y podrá ser utilizado como administrador. El administrador podrá crear, modificar y eliminar (lógicamente) cultivos, y crear y modificar tipos de cultivos.

### Características funcionales del sistema
- Cálculo de la necesidad de agua de riego de un cultivo plantado bajo condiciones óptimas [mm/día].
- Creación, modificación y eliminación (lógica) de parcelas.
- Creación, modificación y eliminación de registros de plantación.
- Creación y modificación de registros de riego.
- Creación, modificación y eliminación de registros climáticos.
- Generación y eliminación de informes estadísticos de parcelas.
- Obtención y persistencia, en forma de registros climáticos, de los datos meteorológicos de la ubicación geográfica de una parcela.
- Registro (sign up) e ingreso (log in) de usuario.
- Ingreso (log in) como administrador. 
- Creación, modificación y eliminación (lógica) de cultivos por parte del administrador.
- Creación y modificación de tipos de cultivos por parte del administrador.
- Modificación de los datos y de la contraseña de la cuenta.