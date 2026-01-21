<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="事故新規登録" />
    <jsp:param name="active" value="accidents" />
</jsp:include>

<h2 class="mb-4">事故新規登録</h2>

<!-- エラーメッセージ（入力不正：errors / システム系：errorMessage） -->
<c:if test="${not empty errors}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <ul class="mb-0">
            <c:forEach var="err" items="${errors}">
                <li>${err}</li>
            </c:forEach>
        </ul>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${errorMessage}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<div class="card">
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/accidents/new">
            <div class="mb-3">
                <label for="policyId" class="form-label">契約 <span class="text-danger">*</span></label>
                <select class="form-select" id="policyId" name="policyId" required>
                    <option value="">選択してください</option>
                    <c:forEach var="policy" items="${policies}">
                        <option value="${policy.id}" ${policyId == policy.id ? 'selected' : ''}>
                            ${policy.policyNumber} - ${policy.customerName}
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="mb-3">
                <label for="occurredAt" class="form-label">事故受付日 <span class="text-danger">*</span></label>
                <input type="date" class="form-control" id="occurredAt" name="occurredAt" 
                       value="${occurredAt}" required>
            </div>

            <div class="mb-3">
                <label for="place" class="form-label">場所</label>
                <input type="text" class="form-control" id="place" name="place" value="${place}">
            </div>

            <div class="mb-3">
                <label for="description" class="form-label">概要</label>
                <textarea class="form-control" id="description" name="description" rows="3">${description}</textarea>
            </div>

            <div class="mt-4">
                <button type="submit" class="btn btn-primary">登録</button>
                <a href="${pageContext.request.contextPath}/accidents" class="btn btn-secondary">キャンセル</a>
            </div>
        </form>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />