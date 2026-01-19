<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="jp.insurance.system.util.DateUtil" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="契約詳細" />
    <jsp:param name="active" value="policies" />
</jsp:include>

<h2 class="mb-4">契約詳細</h2>

<!-- メッセージ表示 -->
<c:if test="${not empty message}">
    <div class="alert alert-${messageType} alert-dismissible fade show" role="alert">
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<c:set var="today" value="<%= DateUtil.today() %>" />

<div class="card mb-4">
    <div class="card-header">
        <h5 class="mb-0">基本情報</h5>
    </div>
    <div class="card-body">
        <table class="table table-borderless">
            <tbody>
                <tr>
                    <th width="150">契約番号</th>
                    <td>${policy.policyNumber}</td>
                </tr>
                <tr>
                    <th>契約者名</th>
                    <td>${policy.customerName}</td>
                </tr>
                <tr>
                    <th>開始日</th>
                    <td>${DateUtil.formatDate(policy.startDate)}</td>
                </tr>
                <tr>
                    <th>満期日</th>
                    <td>${DateUtil.formatDate(policy.endDate)}</td>
                </tr>
                <tr>
                    <th>ステータス</th>
                    <td>
                        ${policyService.getDisplayStatus(policy)}
                        <c:if test="${policyService.isAttentionPeriod(policy, today)}">
                            <span class="badge bg-warning text-dark ms-2">要注意</span>
                        </c:if>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<!-- 操作ボタン -->
<div class="card">
    <div class="card-header">
        <h5 class="mb-0">操作</h5>
    </div>
    <div class="card-body">
        <!-- 更新ボタン -->
        <c:if test="${policyService.isRenewable(policy, today)}">
            <form method="post" action="${pageContext.request.contextPath}/policies/renew" style="display:inline;">
                <input type="hidden" name="id" value="${policy.id}">
                <button type="submit" class="btn btn-primary" 
                        onclick="return confirm('契約を更新しますか？')">更新</button>
            </form>
        </c:if>

        <!-- 更新取消ボタン -->
        <c:if test="${policy.renewedAt != null && policy.renewedAt.toLocalDate().equals(today)}">
            <form method="post" action="${pageContext.request.contextPath}/policies/unrenew" style="display:inline;">
                <input type="hidden" name="id" value="${policy.id}">
                <button type="submit" class="btn btn-warning" 
                        onclick="return confirm('更新を取り消しますか？')">更新取消</button>
            </form>
        </c:if>

        <!-- 解約ボタン -->
        <c:if test="${policy.status.name() == 'ACTIVE'}">
            <form method="post" action="${pageContext.request.contextPath}/policies/cancel" style="display:inline;">
                <input type="hidden" name="id" value="${policy.id}">
                <button type="submit" class="btn btn-danger" 
                        onclick="return confirm('契約を解約しますか？')">解約</button>
            </form>
        </c:if>

        <!-- 解約取消ボタン -->
        <c:if test="${policy.cancelledAt != null && policy.cancelledAt.toLocalDate().equals(today)}">
            <form method="post" action="${pageContext.request.contextPath}/policies/uncancel" style="display:inline;">
                <input type="hidden" name="id" value="${policy.id}">
                <button type="submit" class="btn btn-secondary" 
                        onclick="return confirm('解約を取り消しますか？')">解約取消</button>
            </form>
        </c:if>
    </div>
</div>

<div class="mt-3">
    <a href="${pageContext.request.contextPath}/policies" class="btn btn-outline-secondary">一覧に戻る</a>
</div>

<jsp:include page="../common/footer.jsp" />