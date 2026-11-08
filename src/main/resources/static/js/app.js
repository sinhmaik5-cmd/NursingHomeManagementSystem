/**
 * Nursing Home Management System - Frontend JS
 */

document.addEventListener('DOMContentLoaded', function () {
    // Mobile sidebar toggle
    const toggleBtn = document.getElementById('sidebarToggleBtn');
    const sidebar = document.querySelector('.app-sidebar');
    const overlay = document.getElementById('sidebarOverlay');

    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', function () {
            sidebar.classList.toggle('show');
            if (overlay) overlay.classList.toggle('d-none');
        });
    }

    if (overlay && sidebar) {
        overlay.addEventListener('click', function () {
            sidebar.classList.remove('show');
            overlay.classList.add('d-none');
        });
    }

    // Auto dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) {
                bsAlert.close();
            }
        }, 5000);
    });
});

// Format VND currency helper for client-side calculations
function formatVND(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

/**
 * Generic Table / List Client-Side Pagination Helper with Browser History Support
 * @param {string|HTMLElement} tableSelector - CSS selector or element for the table
 * @param {string} wrapperId - ID of the pagination wrapper div
 * @param {string} paginationUlId - ID of the <ul> pagination element
 * @param {string} infoId - ID of the text info element
 * @param {number} pageSize - Number of items per page (default: 5)
 * @param {string} itemLabel - Noun describing the items (default: 'mục')
 * @param {string} pageParam - URL query param name for browser history (default: 'page')
 */
function initTablePagination(tableSelector, wrapperId, paginationUlId, infoId, pageSize = 5, itemLabel = 'mục', pageParam = 'page') {
    const table = typeof tableSelector === 'string' ? document.querySelector(tableSelector) : tableSelector;
    if (!table) return null;

    const tbody = table.querySelector('tbody');
    if (!tbody) return null;

    let currentPage = 1;
    try {
        const url = new URL(window.location.href);
        const pVal = parseInt(url.searchParams.get(pageParam), 10);
        if (!isNaN(pVal) && pVal >= 1) {
            currentPage = pVal;
        }
    } catch (e) {}

    function getRows() {
        return Array.from(tbody.querySelectorAll('tr')).filter(tr => {
            const hasColspan = tr.querySelector('td[colspan]');
            return !hasColspan && !tr.classList.contains('empty-row');
        });
    }

    function render() {
        const rows = getRows();
        const totalItems = rows.length;
        const totalPages = Math.ceil(totalItems / pageSize) || 1;

        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        const wrapper = document.getElementById(wrapperId);
        const ul = document.getElementById(paginationUlId);
        const info = document.getElementById(infoId);

        if (totalItems === 0) {
            if (wrapper) wrapper.classList.add('d-none');
            return;
        }

        if (wrapper) wrapper.classList.remove('d-none');

        const startIndex = (currentPage - 1) * pageSize;
        const endIndex = Math.min(startIndex + pageSize, totalItems);

        rows.forEach((row, idx) => {
            if (idx >= startIndex && idx < endIndex) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });

        if (info) {
            info.textContent = `Đang hiển thị ${startIndex + 1} - ${endIndex} trong số ${totalItems} ${itemLabel}`;
        }

        if (ul) {
            let html = '';

            // Previous button ("Trước" / Quay lại bước trước)
            html += `<li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
                        <a class="page-link" href="javascript:void(0)" data-page="${currentPage - 1}" aria-label="Trang trước" ${currentPage === 1 ? 'tabindex="-1" aria-disabled="true"' : ''}>
                            <i class="bi bi-chevron-left me-1"></i>Trước
                        </a>
                     </li>`;

            let startPage = Math.max(1, currentPage - 2);
            let endPage = Math.min(totalPages, currentPage + 2);

            if (startPage > 1) {
                html += `<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="1">1</a></li>`;
                if (startPage > 2) {
                    html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
                }
            }

            for (let p = startPage; p <= endPage; p++) {
                if (p === currentPage) {
                    html += `<li class="page-item active" aria-current="page"><a class="page-link fw-bold" href="javascript:void(0)">${p}</a></li>`;
                } else {
                    html += `<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="${p}">${p}</a></li>`;
                }
            }

            if (endPage < totalPages) {
                if (endPage < totalPages - 1) {
                    html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
                }
                html += `<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="${totalPages}">${totalPages}</a></li>`;
            }

            // Next button ("Sau")
            html += `<li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
                        <a class="page-link" href="javascript:void(0)" data-page="${currentPage + 1}" aria-label="Trang sau" ${currentPage === totalPages ? 'tabindex="-1" aria-disabled="true"' : ''}>
                            Sau<i class="bi bi-chevron-right ms-1"></i>
                        </a>
                     </li>`;

            ul.innerHTML = html;

            ul.querySelectorAll('a.page-link[data-page]').forEach(link => {
                link.addEventListener('click', function (e) {
                    e.preventDefault();
                    const targetPage = parseInt(this.getAttribute('data-page'), 10);
                    if (!isNaN(targetPage) && targetPage >= 1 && targetPage <= totalPages && targetPage !== currentPage) {
                        goToPage(targetPage, true);
                    }
                });
            });
        }
    }

    function goToPage(page, pushHistory = true) {
        const rows = getRows();
        const totalPages = Math.ceil(rows.length / pageSize) || 1;
        if (page < 1 || page > totalPages) return;

        currentPage = page;
        render();

        if (pushHistory) {
            try {
                const url = new URL(window.location.href);
                url.searchParams.set(pageParam, page);
                window.history.pushState({ [pageParam]: page }, '', url.toString());
            } catch (err) {
                console.warn('History pushState error:', err);
            }
        }

        table.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    // Support browser Back and Forward history buttons (Quay lại / Tiếp tục các bước)
    window.addEventListener('popstate', function (event) {
        try {
            const url = new URL(window.location.href);
            const paramVal = parseInt(url.searchParams.get(pageParam), 10);
            const historyPage = (event.state && event.state[pageParam]) || paramVal || 1;
            const rows = getRows();
            const totalPages = Math.ceil(rows.length / pageSize) || 1;
            if (historyPage !== currentPage && historyPage >= 1 && historyPage <= totalPages) {
                goToPage(historyPage, false);
            }
        } catch (err) {
            console.warn('History popstate error:', err);
        }
    });

    render();

    return {
        refresh: function() {
            goToPage(1, true);
        },
        goToPage: function(p) {
            goToPage(p, true);
        }
    };
}
