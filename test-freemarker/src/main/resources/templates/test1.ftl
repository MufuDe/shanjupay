<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta charset="utf-8">
    <title>Hello World!</title>
</head>
<body>
Hello ${name}!
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#if stus??>
        <#list stus as stu>
            <tr>
                <td>${stu_index + 1}</td>
                <td <#if stu.name?? && stu.name == '小明'>style="background: red"</#if>>${stu.name}</td>
                <td>${stu.age}</td>
                <td>${stu.money}</td>
            </tr>
        </#list>
    </#if>
</table>
<br/>
输出stu1的学生信息：<br/>
姓名：${(stuMap['stu1'].name)!""}<br/>
年龄：${stuMap['stu1'].age}<br/>
输出stu2的学生信息：<br/>
姓名：${stuMap.stu2.name}<br/>
年龄：${stuMap.stu2.age}<br/>
遍历输出两个学生信息：<br/>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#if stuMap??>
        <#list stuMap?keys as k>
            <tr>
                <td>${k_index + 1}</td>
                <td <#if stuMap[k].name??>style="background: red" </#if>>${stuMap[k].name!""}</td>
                <td>${stuMap[k].age}</td>
                <td >${stuMap[k].money}</td>
            </tr>
        </#list>
    </#if>
</table>
Request属性值是：${Request['attr1']}<br/>
Session属性值是：${Session['session1']}<br/>
contextPath:${rc.contextPath}
</body>
</html>