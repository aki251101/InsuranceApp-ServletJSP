<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="${empty title ? 'ページが見つかりません' : title}" />
<c:set var="activeMenu" value="${empty active ? '' : active}" />

<jsp:include page="header.jsp">
    <jsp:param name="title" value="${pageTitle}" />
    <jsp:param name="active" value="${activeMenu}" />
</jsp:include>

<div class="alert alert-warning" role="alert">
    <c:out value="${empty message ? '対象データが見つかりません。' : message}" />
</div>

<div class="d-flex gap-2">
    <c:choose>
        <c:when test="${not empty backUrl}">
            <a class="btn btn-secondary" href="${backUrl}">一覧へ戻る</a>
        </c:when>
        <c:otherwise>
            <a class="btn btn-secondary" href="javascript:history.back()">戻る</a>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="footer.jsp" />
