<html>
<head>
    <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
    <script type="text/javascript">
        google.charts.load('current', {'packages':['corechart']});
        google.charts.setOnLoadCallback(drawChart);

        function drawChart() {

            var data = google.visualization.arrayToDataTable([
                ${dataset?no_esc}
            ]);

            //Create domain axis specification
            var domainAxis = {
                baselineColor: "${domainAxis["axisBaselineColor"]!"black"}",
                format: "${domainAxis["axisFormat"]!"decimal"}",
                gridlines: {
                    color: "${domainAxis["axisGridLineColor"]!"gray"}",
                    count: ${domainAxis["axisGridLineCount"]!5}
                },
                scaleType: <#if domainAxis["axisScaleType"]??>"${domainAxis["axisScaleType"]}"<#else>null</#if>,
                textPosition: "${domainAxis["axisTextPosition"]!"out"}",
                textStyle: {
                    color: "${domainAxis["axisTickTextColor"]!"black"}",
                    fontName: "${domainAxis["axisTickFontName"]!"Arial"}",
                    fontSize: ${domainAxis["axisTickFontSize"]!"12"},
                    bold: ${domainAxis["axisTickFontBold"]?c},
                    italic: ${domainAxis["axisTickFontItalic"]?c}
                },
                title: <#if domainAxis["axisTitleText"]??>"${domainAxis["axisTitleText"]}"<#else>null</#if>,
                titleTextStyle: {
                    color: "${domainAxis["axisTitleTextColor"]!"black"}",
                    fontName: "${domainAxis["axisTitleFontName"]!"Arial"}",
                    fontSize: ${domainAxis["axisTitleFontSize"]!"12"},
                    bold: ${domainAxis["axisTitleFontBold"]?c},
                    italic: ${domainAxis["axisTitleFontItalic"]?c}
                },
                viewWindowMode: "pretty"
            };

            var rangeAxes = [];

            <#list rangeAxes as rangeAxis>
            rangeAxes[${rangeAxis?index}] = {
                baselineColor: "${rangeAxis["axisBaselineColor"]!"black"}",
                format: "${rangeAxis["axisFormat"]!"decimal"}",
                gridlines: {
                    color: "${rangeAxis["axisGridLineColor"]!"gray"}",
                    count: ${rangeAxis["axisGridLineCount"]!5}
                },
                scaleType: <#if rangeAxis["axisScaleType"]??>"${rangeAxis["axisScaleType"]}"<#else>null</#if>,
                textPosition: "${rangeAxis["axisTextPosition"]!"out"}",
                textStyle: {
                    color: "${rangeAxis["axisTickTextColor"]!"black"}",
                    fontName: "${rangeAxis["axisTickFontName"]!"Arial"}",
                    fontSize: ${rangeAxis["axisTickFontSize"]!"12"},
                    bold: ${rangeAxis["axisTickFontBold"]?c},
                    italic: ${rangeAxis["axisTickFontItalic"]?c}
                },
                title: <#if rangeAxis["axisTitleText"]??>"${rangeAxis["axisTitleText"]}"<#else>null</#if>,
                titleTextStyle: {
                    color: "${rangeAxis["axisTitleTextColor"]!"black"}",
                    fontName: "${rangeAxis["axisTitleFontName"]!"Arial"}",
                    fontSize: ${rangeAxis["axisTitleFontSize"]!"12"},
                    bold: ${rangeAxis["axisTitleFontBold"]?c},
                    italic: ${rangeAxis["axisTitleFontItalic"]?c}
                },
                <#if rangeAxis["axisTicks"]??>
                    ticks: ${rangeAxis["axisTicks"]},
                </#if>
                viewWindowMode: "pretty"
            };
            </#list>

            var axis_horizontal = domainAxis;
            var axis_vertical = rangeAxes[0];
            <#if chartType == "bars" && orientation == "horizontal">
                axis_horizontal = rangeAxes[0];
                axis_vertical = domainAxis;
            </#if>

            var options = {
                title: <#if title??>"${title}"<#else>null</#if>,
                titlePosition: "${titlePosition!"out"}",
                titleTextStyle: {
                    color: "${titleTextColor!"black"}",
                    fontName: "${titleFontName!"Arial"}",
                    fontSize: ${titleFontSize!"12"},
                    bold: ${titleFontBold?c},
                    italic: ${titleFontItalic?c}
                },
                axisTitlesPosition: "${axisTitlesPosition!"out"}",
                backgroundColor: {
                    stroke: "${backgroundColorStroke}",
                    strokeWidth: ${backgroundColorStrokeWidth},
                    fill: "${backgroundColorFill}"
                },
                bar: {
                    groupWidth: "${barGroupWidth!"61.8%"}"
                },
                chartArea: {
                    left: "${chartAreaLeft!"auto"}",
                    top: "${chartAreaTop!"auto"}",
                    width: "${chartAreaWidth!"auto"}",
                    height: "${chartAreaHeight!"auto"}",
                },
                curveType: "${curveType!"None"}",
                hAxis: axis_horizontal,
                vAxis: axis_vertical,
                <#if orientation == 'horizontal' && rangeAxisCount gt 1>
                vAxes: {
                    1: {
                        title:'Losses',
                        textStyle: {color: 'red'}
                    }
                },
                </#if>
                series: {
                    1: {
                        targetAxisIndex: 0
                    }
                },
		        <#if trendLines??>
                trendlines: {
                    <#list trendLines as trendline>
                    ${trendline.seriesIndex}: {
                        type: "linear",
                        color: "${trendline.colorHex}",
                        lineWidth: ${trendline.lineWidth},
                        opacity: ${trendline.opacity},
                        showR2: ${trendline.showR2?c},
                        visibleInLegend: ${trendline.visibleInLegend?c}
                    }<#sep>,</#sep>
                    </#list>
                },
                </#if>
                <#if chartType != "bars">
                orientation: "${orientation}",
                </#if>
                isStacked: ${isStacked?c},
                dataOpacity: ${dataOpacity!1.0},
                fontSize: "${fontSize!"automatic"}",
                fontName: "${fontName!"Arial"}",
                legend: {
                    position: "${legendPosition!"right"}",
                    alignment: "${legendAlignment!"automatic"}",
                    textStyle: {
                        color: "gray",
                        fontName: "Arial",
                        fontSize: 12,
                        bold: false,
                        italic: false
                    }
                }
            };

            <#if chartType == "lines">
                var target = document.getElementById('chart_div');
                var chart = new google.visualization.LineChart(target);
                chart.draw(data, options);
            <#elseif chartType == "bars" && orientation == "vertical">
                var target = document.getElementById('chart_div');
                var chart = new google.visualization.ColumnChart(target);
                chart.draw(data, options);
            <#elseif chartType == "bars" && orientation == "horizontal">
                var target = document.getElementById('chart_div');
                var chart = new google.visualization.BarChart(target);
                chart.draw(data, options);
            <#elseif chartType == "area">
                var target = document.getElementById('chart_div');
                var chart = new google.visualization.AreaChart(target);
                chart.draw(data, options);
            <#elseif chartType == "scatter">
                var target = document.getElementById('chart_div');
                var chart = new google.visualization.ScatterChart(target);
                chart.draw(data, options);
            </#if>

        }
    </script>
</head>

<body>
<div id="chart_div" style="width:100%;height:100%;"></div>
</body>
</html>