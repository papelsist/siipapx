<!doctype html>
<html>
<body>
    <head>
        <style type="text/css" media="screen">
            .zui-table {
                border: solid 1px #DDEEEE;
                border-collapse: collapse;
                border-spacing: 0;
                font: normal 13px Arial, sans-serif;
            }
            .zui-table thead th {
                background-color: #DDEFEF;
                border: solid 1px #DDEEEE;
                color: #336B6B;
                padding: 10px;
                text-align: left;
                text-shadow: 1px 1px 1px #fff;
            }
            .zui-table tbody td {
                border: solid 1px #DDEEEE;
                color: #333;
                padding: 10px;
                text-shadow: 1px 1px 1px #fff;
            }
        </style>
    </head>
    <div>
        <p>Apreciable cliente le hacemos llegar las siguientes facturas electrónicas.<p>
        <table class="zui-table">
            <thead>
                <tr>
                    <th>Tipo De CFDI</th>
                    <th>Serie</th>
                    <th>Folio</th>
                    <th>Fecha</th>
                    <th>Total</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${facturas}" var="row">
                    <tr>
                        <td>${fieldValue(bean:row, field:'tipoDeComprobante')}</td>
                        <td>${fieldValue(bean:row, field:'serie')}</td>
                        <td>${fieldValue(bean:row, field:'folio')}</td>
                        <td><g:formatDate date="${row.fecha}" format="dd/MM/yyyy"/></td>
                        <td><g:formatNumber number="${row.total}" type="currency" currencyCode="MXN"/></td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
    <div>
        <p>
            Este correo se envía de manera autmática favor de no responder a la dirección del mismo.
            Cualquier duda o aclaración
            la puede dirigir a: <a href="mailto:servicioaclientes@papelsa.com.mx">servicio a clientes</a>.
        </p>
    </div>
</body>
</html>

