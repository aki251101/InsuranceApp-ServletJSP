<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="jp.insurance.system.util.DateUtil" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="事故一覧" />
    <jsp:param name="active" value="accidents" />
</jsp:include>

<h2 class="mb-4">事故一覧</h2>

<!-- タブ -->
<ul class="nav nav-tabs mb-3">
    <li class="nav-item">
        <a class="nav-link ${currentTab == 'active' ? 'active' : ''}" 
           href="${pageContext.request.contextPath}/accidents?tab=active">対応中</a>
    </li>
    <li class="nav-item">
        <a class="nav-link ${currentTab == 'resolved' ? 'active' : ''}" 
           href="${pageContext.request.contextPath}/accidents?tab=resolved">完了</a>
    </li>
</ul>

<!-- 新規登録ボタン -->
<div class="mb-3">
    <a href="${pageContext.request.contextPath}/accidents/new" class="btn btn-success">事故新規登録</a>
</div>

<!-- 一覧表 -->
<c:if test="${empty accidents}">
    <div class="alert alert-info">該当する事故がありません。</div>
</c:if>

<c:if test="${not empty accidents}">
    <table class="table table-hover">
        <thead class="table-light">
            <tr>
                <th>事故受付日</th>
                <th>契約番号</th>
                <th>契約者名</th>
                <th>ステータス</th>
                <th>滞留</th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="accident" items="${accidents}">
                <tr>
                    <td>${DateUtil.formatDate(accident.occurredAt)}</td>
                    <td>${accident.policyNumber}</td>
                    <td>${accident.customerName}</td>
                    <td>${accident.status.displayName}</td>
                    <td>
                        <c:if test="${accidentService.isStagnant(accident)}">
                            <span class="badge bg-danger">滞留</span>
                        </c:if>
                    </td>
                    <td>
                        <a href="${pageContext.request.contextPath}/accidents/detail?id=${accident.id}" 
                           class="btn btn-sm btn-primary">詳細</a>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</c:if>

<jsp:include page="../common/footer.jsp" />