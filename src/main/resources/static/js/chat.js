/**
 * In-App Chat/Messaging System
 * Handles real-time messaging between customers and support
 */

class ChatSystem {
    constructor(chatId, userEmail) {
        this.chatId = chatId;
        this.userEmail = userEmail;
        this.stompClient = null;
        this.isConnected = false;
        this.messages = [];
    }

    async init() {
        await this.loadChatHistory();
        this.connectWebSocket();
        this.setupUI();
    }

    connectWebSocket() {
        if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            console.warn('WebSocket libraries not loaded. Using REST fallback.');
            return;
        }

        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect({}, (frame) => {
            console.log('Chat WebSocket connected:', frame);
            this.isConnected = true;
            this.subscribeToChat();
            this.updateConnectionStatus(true);
        }, (error) => {
            console.error('Chat WebSocket connection error:', error);
            this.isConnected = false;
            this.updateConnectionStatus(false);
        });
    }

    subscribeToChat() {
        if (!this.stompClient || !this.isConnected) return;

        this.stompClient.subscribe('/topic/chat/' + this.chatId, (message) => {
            const data = JSON.parse(message.body);
            this.addMessage(data);
        });
    }

    async loadChatHistory() {
        try {
            const response = await fetch(`/api/chat/history/${this.chatId}`);
            const result = await response.json();

            if (result.success && result.messages) {
                this.messages = result.messages;
                this.displayMessages();
            }
        } catch (error) {
            console.error('Error loading chat history:', error);
        }
    }

    sendMessage(text) {
        if (!text || text.trim().length === 0) return;

        const message = {
            chatId: this.chatId,
            senderEmail: this.userEmail,
            text: text.trim(),
            timestamp: new Date().toISOString()
        };

        // Try WebSocket first
        if (this.isConnected && this.stompClient) {
            this.stompClient.send('/app/send', {}, JSON.stringify(message));
        } else {
            // Fallback to REST API
            this.sendMessageRest(message);
        }

        // Clear input
        const messageInput = document.getElementById('chatMessageInput');
        if (messageInput) {
            messageInput.value = '';
        }
    }

    async sendMessageRest(message) {
        try {
            await fetch('/api/chat/send', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(message)
            });
        } catch (error) {
            console.error('Error sending message via REST:', error);
            this.showError('Failed to send message. Please try again.');
        }
    }

    addMessage(messageData) {
        this.messages.push(messageData);
        this.displayMessage(messageData);
        this.scrollToBottom();
    }

    displayMessages() {
        const messagesContainer = document.getElementById('chatMessages');
        if (!messagesContainer) return;

        messagesContainer.innerHTML = '';
        this.messages.forEach(message => {
            this.displayMessage(message);
        });
        this.scrollToBottom();
    }

    displayMessage(message) {
        const messagesContainer = document.getElementById('chatMessages');
        if (!messagesContainer) return;

        const isOwnMessage = message.senderEmail === this.userEmail;
        const messageClass = isOwnMessage ? 'bg-blue-500 text-white ml-auto' : 'bg-gray-200 text-gray-900';
        const alignment = isOwnMessage ? 'justify-end' : 'justify-start';

        const messageDiv = document.createElement('div');
        messageDiv.className = `flex ${alignment} mb-4`;
        messageDiv.innerHTML = `
            <div class="${messageClass} rounded-lg px-4 py-2 max-w-xs lg:max-w-md">
                <div class="text-sm font-medium mb-1">${message.senderEmail}</div>
                <div class="text-sm">${this.escapeHtml(message.text)}</div>
                <div class="text-xs mt-1 opacity-75">${this.formatTime(message.timestamp)}</div>
            </div>
        `;

        messagesContainer.appendChild(messageDiv);
    }

    setupUI() {
        const sendButton = document.getElementById('chatSendButton');
        const messageInput = document.getElementById('chatMessageInput');

        if (sendButton) {
            sendButton.addEventListener('click', () => {
                if (messageInput) {
                    this.sendMessage(messageInput.value);
                }
            });
        }

        if (messageInput) {
            messageInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    this.sendMessage(messageInput.value);
                }
            });
        }
    }

    scrollToBottom() {
        const messagesContainer = document.getElementById('chatMessages');
        if (messagesContainer) {
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }
    }

    updateConnectionStatus(connected) {
        const statusIndicator = document.getElementById('chatConnectionStatus');
        if (statusIndicator) {
            statusIndicator.className = connected 
                ? 'w-2 h-2 bg-green-500 rounded-full mr-2' 
                : 'w-2 h-2 bg-red-500 rounded-full mr-2';
            statusIndicator.title = connected ? 'Connected' : 'Disconnected';
        }
    }

    formatTime(timestamp) {
        if (!timestamp) return '';
        const date = new Date(timestamp);
        return date.toLocaleTimeString('en-ZA', { hour: '2-digit', minute: '2-digit' });
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    showError(message) {
        // Create error toast
        const toast = document.createElement('div');
        toast.className = 'fixed bottom-4 right-4 bg-red-500 text-white px-6 py-3 rounded-lg shadow-lg z-50';
        toast.textContent = message;
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.remove();
        }, 3000);
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}

// Global chat instance
let chatSystem = null;

// Initialize chat
async function initChat(chatId, userEmail) {
    if (chatSystem) {
        chatSystem.disconnect();
    }
    chatSystem = new ChatSystem(chatId, userEmail);
    await chatSystem.init();
}

// Initiate new chat
async function initiateChat(customerEmail, trackingNumber, subject) {
    try {
        const response = await fetch('/api/chat/initiate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                customerEmail: customerEmail,
                trackingNumber: trackingNumber,
                subject: subject
            })
        });

        const result = await response.json();

        if (result.success && result.chatId) {
            await initChat(result.chatId, customerEmail);
            return result.chatId;
        } else {
            throw new Error(result.message || 'Failed to initiate chat');
        }
    } catch (error) {
        console.error('Error initiating chat:', error);
        throw error;
    }
}
