<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="契約新規登録" />
    <jsp:param name="active" value="policies" />
</jsp:include>

<h2 class="mb-4">契約新規登録</h2>

<!-- エラーメッセージ -->
<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        ${errorMessage}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<div class="card">
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/policies/new">
            <div class="mb-3">
                <label for="policyNumber" class="form-label">契約番号 <span class="text-danger">*</span></label>
                <input type="text" class="form-control" id="policyNumber" name="policyNumber" 
                       value="${policyNumber}" required>
            </div>

            <div class="mb-3">
                <label for="customerName" class="form-label">契約者名 <span class="text-danger">*</span></label>
                <input type="text" class="form-control" id="customerName" name="customerName" 
                       value="${customerName}" required>
            </div>

            <div class="mb-3">
                <label for="startDate" class="form-label">開始日 <span class="text-danger">*</span></label>
                <input type="date" class="form-control" id="startDate" name="startDate" 
                       value="${startDate}" required>
            </div>

            <div class="mb-3">
                <label for="endDate" class="form-label">満期日 <span class="text-danger">*</span></label>
                <input type="date" class="form-control" id="endDate" name="endDate" 
                       value="${endDate}" required>
            </div>

            <div class="mt-4">
                <button type="submit" class="btn btn-primary">登録</button>
                <a href="${pageContext.request.contextPath}/policies" class="btn btn-secondary">キャンセル</a>
            </div>
        </form>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />