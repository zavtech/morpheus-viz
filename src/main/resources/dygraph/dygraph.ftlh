<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Dygraph Example</title>
    <script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/dygraph/1.1.1/dygraph-combined.js"></script>
</head>
<body>

<div id="graph" style="width:95%; height:600px;"></div>

<script type="text/javascript">
  g2 = new Dygraph(document.getElementById("graph"),
    [
        ${model?no_esc}
    ],
    {
        labels: [ ${labels?no_esc} ],
        <#if title??>title: "${title}",</#if>
        <#if xAxisLabel??>xlabel: "${xAxisLabel}",</#if>
        <#if y1AxisLabel??>ylabel: "${y1AxisLabel}",</#if>
        <#if y2AxisLabel??>y2Label: "${y2AxisLabel}"</#if>
    }
  );
</script>
</body>
</html>