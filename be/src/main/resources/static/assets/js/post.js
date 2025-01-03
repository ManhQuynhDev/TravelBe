$(document).ready(function () {
    var table = $('#modernTable1').DataTable({
        dom: '<"row d-flex justify-content-between align-items-center"<"col-sm-6 d-flex align-items-center"f><"col-sm-6 d-flex justify-content-end"B>>' +
            '<"row"<"col-sm-12"tr>>' +
            '<"row d-flex justify-content-between align-items-center"<"col-sm-6"i><"col-sm-6 d-flex justify-content-end"p>>',
        buttons: ['copy', 'csv', 'excel', 'pdf', 'print'],
        paging: true,
        searching: true,
        ordering: true,
        order: [],
        pageLength: 5,
        columnDefs: [
            {
                targets: [6],
                orderable: false
            }
        ],
        language: {
            search: "",
            searchPlaceholder: "Tìm kiếm bài viết",
            paginate: {
                previous: "Trước",
                next: "Tiếp theo",
            },
            emptyTable: "Không có dữ liệu trong bảng",
            info: "Hiển thị từ _START_ đến _END_ của _TOTAL_ mục",
            infoEmpty: "Không có dữ liệu",
            infoFiltered: "(lọc từ _MAX_ mục)",
            lengthMenu: "Hiển thị _MENU_ mục",
        }
    });

    $('#selectAll').on('click', function () {
        var rows = table.rows({ 'search': 'applied' }).nodes();
        $('input[type="checkbox"]', rows).prop('checked', this.checked);
    });

    $('#modernTable1 tbody').on('change', 'input[type="checkbox"]', function () {
        if (!this.checked) {
            var el = $('#selectAll').get(0);
            if (el && el.checked) {
                el.indeterminate = true;
            }
        }
    });
});
const token = localStorage.getItem('authToken');
function loadPostDetails(id) {
    fetch(`http://localhost:8080/api/posts/${id}/1`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(res => res.json())
        .then(data => {
            if (data.status) {
                const post = data.data;

                const mediaContainer = document.getElementById('mediaContainer');
                mediaContainer.innerHTML = '';
                if (post.mediaUrls && post.mediaUrls.length > 0) {
                    post.mediaUrls.forEach(url => {
                        const img = document.createElement('img');
                        img.src = url;
                        img.onerror = function () {
                            this.src = '/assets/img/anhload.jpg';
                        }
                        img.className = 'img-fluid rounded mb-2';
                        img.style.width = '200px';
                        img.style.height = '112px';
                        img.style.objectFit = 'cover';
                        mediaContainer.appendChild(img);
                    });
                }

                document.getElementById('postContentDetails').innerText = post.content || 'No content available';

                const date = new Date(post.create_time);
                if (!isNaN(date.getTime())) {
                    const day = String(date.getDate()).padStart(2, '0');
                    const month = String(date.getMonth() + 1).padStart(2, '0');
                    const year = date.getFullYear();
                    const hours = String(date.getHours()).padStart(2, '0');
                    const minutes = String(date.getMinutes()).padStart(2, '0');
                    const seconds = String(date.getSeconds()).padStart(2, '0');
                    document.getElementById('postCreatedTimeDetails').innerText = `${day}-${month}-${year} ${hours}:${minutes}:${seconds}`;
                } else {
                    document.getElementById('postCreatedTimeDetails').innerText = 'No time available';
                }

                document.getElementById('postLocationDetails').innerText = post.location || 'No location available';
                document.getElementById('postStatusDetails').innerText = post.status || 'No status available';
                document.getElementById('postFullnameDetails').innerText = post.fullname || 'Anonymous';
                document.getElementById('postReactionCountDetails').innerText = post.reaction_count || '0';
                document.getElementById('postCommentCountDetails').innerText = post.comment_count || '0';

                const hashtagsContainer = document.getElementById('postHashtagsDetails');
                hashtagsContainer.innerHTML = '';
                if (post.hashtags && post.hashtags.length > 0) {
                    hashtagsContainer.innerText = post.hashtags.map(tag => `#${tag}`).join(', ');
                } else {
                    hashtagsContainer.innerText = 'No hashtags available';
                }

                const modal = new bootstrap.Modal(document.getElementById("postDetailModal"));
                modal.show();
            } else {
                console.log("Error: " + data.message);
            }
        })
        .catch(error => {
            console.error("Failed: ", error);
        });
}

const apiURL = 'http://localhost:8080/locations?page=0&size=100';
const locationSelect = document.getElementById('postLocation');

if (!token) {
    console.error("Không tìm thấy token trong localStorage");
} else {
    async function fetchLocations() {
        try {
            const response = await fetch(apiURL, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Lỗi HTTP! Mã trạng thái: ${response.status}`);
            }

            const data = await response.json();
            console.log("Danh sách Vị Trí:", data);

            locationSelect.innerHTML = '<option value="">Chọn Vị Trí</option>';

            if (data.content && Array.isArray(data.content)) {
                data.content.forEach(location => {
                    const option = document.createElement('option');
                    option.value = location.address;
                    option.textContent = location.address;
                    locationSelect.appendChild(option);
                });
            }
        } catch (error) {
            console.error('Lỗi khi lấy dữ liệu vị trí:', error);
        }
    }

    fetchLocations();
}
document.addEventListener("DOMContentLoaded", function () {
    const selectedPostId = localStorage.getItem('selectedPostId');

    if (selectedPostId) {
        // Gọi hàm loadGroupDetails với ID nhóm đã lưu trong localStorage
        loadPostDetails(selectedPostId);

        // Sau khi mở modal, xóa ID khỏi localStorage
        localStorage.removeItem('selectedPostId');
    }
});





//delete posst
document.querySelectorAll('#deletePost').forEach(button => {
    button.addEventListener("click", function () {
        const post_id = this.getAttribute("data-id");
        console.log(post_id);

        const modal = new bootstrap.Modal(document.getElementById("deletePostModal"));
        modal.show();

        const confirmButton = document.getElementById("confirmDelete");

        confirmButton.replaceWith(confirmButton.cloneNode(true));
        const newConfirmButton = document.getElementById("confirmDelete");

        newConfirmButton.addEventListener("click", () => {
            fetch(`http://localhost:8080/api/posts/${post_id}`, {
                method: "DELETE",
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
                .then(res => res.json())
                .then(data => {
                    if (data.message === "Delete post successfully") {
                        alert("Xóa bài viết thành công");
                        modal.hide();

                        const storyRow = button.closest('tr');
                        if (storyRow) {
                            storyRow.remove();
                        }
                    } else {
                        alert("Failed to delete post");
                    }
                })
                .catch(error => {
                    console.log(error);
                })
        })
    })
})


document.getElementById("logoutLink").addEventListener("click", function () {
    window.location.replace("/web-server/login");
});

// viewComent
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".viewComments").forEach(button => {
        button.addEventListener("click", function () {
            const postId = this.getAttribute("data-id");

            fetch(`http://localhost:8080/api/comments/postId?postId=${postId}&userId=1`, {
                method: "GET",
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            })
                .then(response => {
                    return response.json();
                })
                .then(data => {
                    const commentsListTable = document.getElementById("commentsListTable");
                    commentsListTable.innerHTML = '';

                    if (data.content.length === 0) {
                        commentsListTable.innerHTML = '<tr><td colspan="4" style="text-align: center;">Không có dữ liệu</td></tr>';
                    } else {
                        data.content.forEach((comment, index) => {
                            const row = document.createElement("tr");
                            const avatarUrl = comment.avatar;
                            const avatar = comment.avatar ?
                                `<img src="${avatarUrl}" alt="${comment.fullname}" style="width: 40px; height: 40px; border-radius: 50%;object-fit: cover; margin-right: 10px;">` :
                                `<div style="width: 40px; height: 40px; border-radius: 50%; background-color: #ddd; display: flex; justify-content: center; align-items: center; margin-right: 10px;">
                        ${comment.fullname.charAt(0).toUpperCase()}
                    </div>`;

                            row.innerHTML = `
                    <td>${index + 1}</td>
                    <td style="display: flex; align-items: center;">
                        ${avatar}
                        ${comment.fullname}
                    </td>
                    <td>${comment.content}</td>
                    <td>${comment.create_time}</td>
                `;

                            commentsListTable.appendChild(row);
                        });
                    }

                    $('#commentsModal').modal('show');
                })
                .catch(error => {
                    console.error('Error fetching comments:', error);
                    alert('Có lỗi xảy ra khi tải bình luận.');
                });
        });
    });
});



