// Admin Verification Management JavaScript

let currentTab = 'driver-docs';

// Get authentication token
function getToken() {
    return localStorage.getItem('token') || 
           sessionStorage.getItem('token') || 
           localStorage.getItem('jwtToken') || 
           sessionStorage.getItem('jwtToken') || '';
}

// Get headers with authentication
function getAuthHeaders() {
    const token = getToken();
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
    };
}

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    loadSummary();
    loadDriverDocuments();
    
    // Auto-refresh every 30 seconds
    setInterval(() => {
        loadSummary();
        if (currentTab === 'driver-docs') loadDriverDocuments();
        else if (currentTab === 'business-docs') loadBusinessDocuments();
        else if (currentTab === 'business-accounts') loadPendingBusinesses();
    }, 30000);
});

// Tab switching
function showTab(tabName) {
    currentTab = tabName;
    
    // Update tab buttons
    document.querySelectorAll('.tab-button').forEach(btn => {
        btn.classList.remove('active', 'border-reliable-600', 'text-reliable-600', 'dark:text-reliable-400');
        btn.classList.add('border-transparent', 'text-gray-500', 'dark:text-gray-400');
    });
    
    const activeBtn = document.getElementById(`tab-${tabName}`);
    if (activeBtn) {
        activeBtn.classList.add('active', 'border-reliable-600', 'text-reliable-600', 'dark:text-reliable-400');
        activeBtn.classList.remove('border-transparent', 'text-gray-500', 'dark:text-gray-400');
    }
    
    // Update content
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.add('hidden');
    });
    
    const activeContent = document.getElementById(`content-${tabName}`);
    if (activeContent) {
        activeContent.classList.remove('hidden');
    }
    
    // Load appropriate data
    if (tabName === 'driver-docs') loadDriverDocuments();
    else if (tabName === 'business-docs') loadBusinessDocuments();
    else if (tabName === 'business-accounts') loadPendingBusinesses();
}

// Load summary statistics
async function loadSummary() {
    try {
        const response = await fetch('/api/admin/verification/pending', {
            headers: getAuthHeaders()
        });
        const data = await response.json();
        
        if (data.success) {
            const pending = data.pending;
            document.getElementById('pendingBadge').textContent = 
                (pending.driverDocuments || 0) + (pending.businessDocuments || 0) + 
                (pending.pendingDrivers || 0) + (pending.pendingBusinesses || 0);
            document.getElementById('pendingDrivers').textContent = pending.pendingDrivers || 0;
            document.getElementById('pendingBusinesses').textContent = pending.pendingBusinesses || 0;
            document.getElementById('pendingDocuments').textContent = 
                (pending.driverDocuments || 0) + (pending.businessDocuments || 0);
        }
    } catch (error) {
        console.error('Error loading summary:', error);
    }
}

// Load driver documents
async function loadDriverDocuments() {
    try {
        const response = await fetch('/api/admin/verification/drivers/pending-documents', {
            headers: getAuthHeaders()
        });
        const data = await response.json();
        
        const container = document.getElementById('driverDocumentsList');
        if (!data.success || !data.documents || data.documents.length === 0) {
            container.innerHTML = '<div class="text-center py-8 text-gray-500 dark:text-gray-400"><i class="fas fa-check-circle text-3xl mb-2"></i><p>No pending driver documents</p></div>';
            return;
        }
        
        container.innerHTML = data.documents.map(doc => {
            const driverNameEscaped = (doc.driverName || '').replace(/'/g, "\\'").replace(/"/g, '&quot;');
            const docTypeEscaped = (doc.documentTypeName || '').replace(/'/g, "\\'").replace(/"/g, '&quot;');
            return `
            <div class="border border-gray-200 dark:border-gray-700 rounded-lg p-4 hover:shadow-md transition-shadow">
                <div class="flex justify-between items-start">
                    <div class="flex-1">
                        <div class="flex items-center space-x-3 mb-2">
                            <span class="px-2 py-1 bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 text-xs font-semibold rounded">
                                ${doc.documentTypeName || 'N/A'}
                            </span>
                            ${doc.isCertified ? '<span class="px-2 py-1 bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 text-xs font-semibold rounded"><i class="fas fa-certificate mr-1"></i>Certified</span>' : ''}
                        </div>
                        <h4 class="font-semibold text-gray-900 dark:text-white">${doc.driverName || 'N/A'}</h4>
                        <p class="text-sm text-gray-600 dark:text-gray-400">${doc.driverEmail || 'N/A'}</p>
                        <p class="text-sm text-gray-600 dark:text-gray-400">${doc.driverPhone || 'N/A'}</p>
                        <p class="text-xs text-gray-500 dark:text-gray-500 mt-2">
                            <i class="fas fa-file mr-1"></i>${doc.fileName || 'N/A'}
                            ${doc.certifiedBy ? ` | Certified by: ${doc.certifiedBy}` : ''}
                        </p>
                    </div>
                    <div class="flex space-x-2">
                        <button onclick="viewDocument('driver', ${doc.id})" class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded">
                            <i class="fas fa-eye mr-1"></i>View
                        </button>
                        <button onclick="openVerificationModal('driver', ${doc.id}, '${driverNameEscaped}', '${docTypeEscaped}')" class="px-3 py-1 bg-green-600 hover:bg-green-700 text-white text-sm rounded">
                            <i class="fas fa-check mr-1"></i>Verify
                        </button>
                    </div>
                </div>
            </div>
        `;
        }).join('');
    } catch (error) {
        console.error('Error loading driver documents:', error);
        document.getElementById('driverDocumentsList').innerHTML = 
            '<div class="text-center py-8 text-red-500"><i class="fas fa-exclamation-triangle text-3xl mb-2"></i><p>Error loading documents</p></div>';
    }
}

// Load business documents
async function loadBusinessDocuments() {
    try {
        const response = await fetch('/api/admin/verification/businesses/pending-documents', {
            headers: getAuthHeaders()
        });
        const data = await response.json();
        
        const container = document.getElementById('businessDocumentsList');
        if (!data.success || !data.documents || data.documents.length === 0) {
            container.innerHTML = '<div class="text-center py-8 text-gray-500 dark:text-gray-400"><i class="fas fa-check-circle text-3xl mb-2"></i><p>No pending business documents</p></div>';
            return;
        }
        
        container.innerHTML = data.documents.map(doc => {
            const businessNameEscaped = (doc.businessName || '').replace(/'/g, "\\'").replace(/"/g, '&quot;');
            const docTypeEscaped = (doc.documentTypeName || '').replace(/'/g, "\\'").replace(/"/g, '&quot;');
            return `
            <div class="border border-gray-200 dark:border-gray-700 rounded-lg p-4 hover:shadow-md transition-shadow">
                <div class="flex justify-between items-start">
                    <div class="flex-1">
                        <div class="flex items-center space-x-3 mb-2">
                            <span class="px-2 py-1 bg-purple-100 dark:bg-purple-900 text-purple-800 dark:text-purple-200 text-xs font-semibold rounded">
                                ${doc.documentTypeName || 'N/A'}
                            </span>
                            ${doc.isCertified ? '<span class="px-2 py-1 bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 text-xs font-semibold rounded"><i class="fas fa-certificate mr-1"></i>Certified</span>' : ''}
                        </div>
                        <h4 class="font-semibold text-gray-900 dark:text-white">${doc.businessName || 'N/A'}</h4>
                        <p class="text-sm text-gray-600 dark:text-gray-400">${doc.businessEmail || 'N/A'}</p>
                        <p class="text-sm text-gray-600 dark:text-gray-400">Reg: ${doc.registrationNumber || 'N/A'}</p>
                        <p class="text-xs text-gray-500 dark:text-gray-500 mt-2">
                            <i class="fas fa-file mr-1"></i>${doc.fileName || 'N/A'}
                            ${doc.certifiedBy ? ` | Certified by: ${doc.certifiedBy}` : ''}
                        </p>
                    </div>
                    <div class="flex space-x-2">
                        <button onclick="viewDocument('business', ${doc.id})" class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded">
                            <i class="fas fa-eye mr-1"></i>View
                        </button>
                        <button onclick="openVerificationModal('business', ${doc.id}, '${businessNameEscaped}', '${docTypeEscaped}')" class="px-3 py-1 bg-green-600 hover:bg-green-700 text-white text-sm rounded">
                            <i class="fas fa-check mr-1"></i>Verify
                        </button>
                    </div>
                </div>
            </div>
        `;
        }).join('');
    } catch (error) {
        console.error('Error loading business documents:', error);
        document.getElementById('businessDocumentsList').innerHTML = 
            '<div class="text-center py-8 text-red-500"><i class="fas fa-exclamation-triangle text-3xl mb-2"></i><p>Error loading documents</p></div>';
    }
}

// Load pending businesses
async function loadPendingBusinesses() {
    try {
        // Get all users and filter for businesses that are pending verification
        const response = await fetch('/api/admin/users', {
            headers: getAuthHeaders()
        });
        const data = await response.json();
        
        // Filter for businesses that are pending verification
        const businesses = Array.isArray(data) ? data.filter(b => 
            b.isBusiness && 
            (b.businessVerificationStatus === null || 
             b.businessVerificationStatus === 'PENDING' || 
             b.businessVerificationStatus === 'UNDER_REVIEW')
        ) : [];
        
        const container = document.getElementById('pendingBusinessesList');
        if (!businesses || businesses.length === 0) {
            container.innerHTML = '<div class="text-center py-8 text-gray-500 dark:text-gray-400"><i class="fas fa-check-circle text-3xl mb-2"></i><p>No pending business accounts</p></div>';
            return;
        }
        
        container.innerHTML = businesses.map(business => `
            <div class="border border-gray-200 dark:border-gray-700 rounded-lg p-4 hover:shadow-md transition-shadow">
                <div class="flex justify-between items-start">
                    <div class="flex-1">
                        <h4 class="font-semibold text-gray-900 dark:text-white">${business.businessName || 'N/A'}</h4>
                        <p class="text-sm text-gray-600 dark:text-gray-400">${business.email}</p>
                        <p class="text-sm text-gray-600 dark:text-gray-400">${business.phone || 'N/A'}</p>
                        <p class="text-xs text-gray-500 dark:text-gray-500 mt-1">
                            Status: ${business.businessVerificationStatus || 'PENDING'}
                        </p>
                    </div>
                    <div class="flex space-x-2">
                        <button onclick="viewBusinessDetails(${business.id})" class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded">
                            <i class="fas fa-eye mr-1"></i>View Details
                        </button>
                        <button onclick="openBusinessVerificationModal(${business.id}, '${(business.businessName || business.email).replace(/'/g, "\\'")}')" class="px-3 py-1 bg-green-600 hover:bg-green-700 text-white text-sm rounded">
                            <i class="fas fa-check mr-1"></i>Approve
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading pending businesses:', error);
        document.getElementById('pendingBusinessesList').innerHTML = 
            '<div class="text-center py-8 text-red-500"><i class="fas fa-exclamation-triangle text-3xl mb-2"></i><p>Error loading businesses</p></div>';
    }
}

// Open verification modal
function openVerificationModal(type, documentId, entityName, documentType) {
    document.getElementById('modalTitle').textContent = `Verify ${documentType} - ${entityName}`;
    document.getElementById('modalContent').innerHTML = `
        <div class="space-y-4">
            <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Decision</label>
                <select id="verificationDecision" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md dark:bg-gray-700 dark:text-white">
                    <option value="approve">Approve</option>
                    <option value="reject">Reject</option>
                </select>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Notes</label>
                <textarea id="verificationNotes" rows="4" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md dark:bg-gray-700 dark:text-white" placeholder="Add verification notes..."></textarea>
            </div>
            <div class="flex justify-end space-x-3 pt-4">
                <button onclick="closeVerificationModal()" class="px-4 py-2 bg-gray-300 dark:bg-gray-600 text-gray-700 dark:text-gray-300 rounded hover:bg-gray-400 dark:hover:bg-gray-500">
                    Cancel
                </button>
                <button onclick="submitVerification('${type}', ${documentId})" class="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded">
                    Submit
                </button>
            </div>
        </div>
    `;
    document.getElementById('verificationModal').classList.remove('hidden');
}

// Submit verification
async function submitVerification(type, documentId) {
    const decision = document.getElementById('verificationDecision').value;
    const notes = document.getElementById('verificationNotes').value;
    
    try {
        const endpoint = type === 'driver' 
            ? `/api/admin/verification/drivers/documents/${documentId}/verify`
            : `/api/admin/verification/businesses/documents/${documentId}/verify`;
        
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                approved: decision === 'approve',
                notes: notes
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            alert(decision === 'approve' ? 'Document approved successfully' : 'Document rejected');
            closeVerificationModal();
            if (type === 'driver') loadDriverDocuments();
            else loadBusinessDocuments();
            loadSummary();
        } else {
            alert('Error: ' + (data.error || 'Failed to verify document'));
        }
    } catch (error) {
        console.error('Error submitting verification:', error);
        alert('Error submitting verification');
    }
}

// View document
function viewDocument(type, documentId) {
    const token = getToken();
    const endpoint = type === 'driver'
        ? `/api/admin/verification/drivers/documents/${documentId}/download`
        : `/api/admin/verification/businesses/documents/${documentId}/download`;
    
    // Create a temporary link with authentication
    const link = document.createElement('a');
    link.href = endpoint;
    link.target = '_blank';
    
    // For downloads, we need to fetch with auth and create a blob
    fetch(endpoint, {
        headers: {
            'Authorization': token ? `Bearer ${token}` : ''
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to load document');
        }
        return response.blob();
    })
    .then(blob => {
        const url = window.URL.createObjectURL(blob);
        link.href = url;
        link.download = ''; // Let browser determine filename
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    })
    .catch(error => {
        console.error('Error viewing document:', error);
        alert('Error loading document. Please try again.');
    });
}

// View business details
async function viewBusinessDetails(businessId) {
    try {
        const response = await fetch(`/api/admin/verification/businesses/${businessId}/details`, {
            headers: getAuthHeaders()
        });
        const data = await response.json();
        
        if (data.success) {
            const business = data.business;
            document.getElementById('modalTitle').textContent = `Business Details - ${business.businessName}`;
            document.getElementById('modalContent').innerHTML = `
                <div class="space-y-4">
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Business Name</label>
                            <p class="text-gray-900 dark:text-white">${business.businessName}</p>
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Registration Number</label>
                            <p class="text-gray-900 dark:text-white">${business.registrationNumber || 'N/A'}</p>
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Tax ID</label>
                            <p class="text-gray-900 dark:text-white">${business.taxId || 'N/A'}</p>
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Status</label>
                            <p class="text-gray-900 dark:text-white">${business.verificationStatus || 'PENDING'}</p>
                        </div>
                    </div>
                    <div>
                        <h4 class="font-semibold text-gray-900 dark:text-white mb-2">Documents</h4>
                        <div class="space-y-2">
                            ${business.documents.map(doc => `
                                <div class="flex justify-between items-center p-2 bg-gray-50 dark:bg-gray-700 rounded">
                                    <span class="text-sm">${doc.documentTypeName} - ${doc.verificationStatus}</span>
                                    <button onclick="viewDocument('business', ${doc.id})" class="text-blue-600 hover:text-blue-800">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                </div>
                            `).join('')}
                        </div>
                    </div>
                    <div class="flex justify-end space-x-3 pt-4">
                        <button onclick="closeVerificationModal()" class="px-4 py-2 bg-gray-300 dark:bg-gray-600 text-gray-700 dark:text-gray-300 rounded hover:bg-gray-400 dark:hover:bg-gray-500">
                            Close
                        </button>
                        ${business.hasAllRequiredDocuments ? `
                            <button onclick="openBusinessVerificationModal(${businessId}, '${business.businessName}')" class="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded">
                                Approve Business
                            </button>
                        ` : ''}
                    </div>
                </div>
            `;
            document.getElementById('verificationModal').classList.remove('hidden');
        }
    } catch (error) {
        console.error('Error loading business details:', error);
        alert('Error loading business details');
    }
}

// Open business verification modal
function openBusinessVerificationModal(businessId, businessName) {
    document.getElementById('modalTitle').textContent = `Verify Business - ${businessName}`;
    document.getElementById('modalContent').innerHTML = `
        <div class="space-y-4">
            <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Decision</label>
                <select id="businessVerificationDecision" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md dark:bg-gray-700 dark:text-white">
                    <option value="approve">Approve</option>
                    <option value="reject">Reject</option>
                </select>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Credit Limit (ZAR)</label>
                <input type="number" id="creditLimit" value="50000" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md dark:bg-gray-700 dark:text-white">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Payment Terms (days)</label>
                <input type="number" id="paymentTerms" value="30" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md dark:bg-gray-700 dark:text-white">
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Notes</label>
                <textarea id="businessVerificationNotes" rows="4" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md dark:bg-gray-700 dark:text-white" placeholder="Add verification notes..."></textarea>
            </div>
            <div class="flex justify-end space-x-3 pt-4">
                <button onclick="closeVerificationModal()" class="px-4 py-2 bg-gray-300 dark:bg-gray-600 text-gray-700 dark:text-gray-300 rounded hover:bg-gray-400 dark:hover:bg-gray-500">
                    Cancel
                </button>
                <button onclick="submitBusinessVerification(${businessId})" class="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded">
                    Submit
                </button>
            </div>
        </div>
    `;
    document.getElementById('verificationModal').classList.remove('hidden');
}

// Submit business verification
async function submitBusinessVerification(businessId) {
    const decision = document.getElementById('businessVerificationDecision').value;
    const creditLimit = document.getElementById('creditLimit').value;
    const paymentTerms = document.getElementById('paymentTerms').value;
    const notes = document.getElementById('businessVerificationNotes').value;
    
    try {
        const response = await fetch(`/api/admin/verification/businesses/${businessId}/verify`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                approved: decision === 'approve',
                notes: notes,
                creditLimit: creditLimit,
                paymentTerms: paymentTerms
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            alert(decision === 'approve' ? 'Business approved successfully' : 'Business rejected');
            closeVerificationModal();
            loadPendingBusinesses();
            loadSummary();
        } else {
            alert('Error: ' + (data.error || 'Failed to verify business'));
        }
    } catch (error) {
        console.error('Error submitting business verification:', error);
        alert('Error submitting verification');
    }
}

// Close modal
function closeVerificationModal() {
    document.getElementById('verificationModal').classList.add('hidden');
}


