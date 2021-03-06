package com.fridaytech.zrxkit.contracts

import com.fridaytech.zrxkit.utils.Constants.MAX_ALLOWANCE
import io.reactivex.Flowable
import java.math.BigInteger
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tx.Contract
import org.web3j.tx.gas.ContractGasProvider

internal class Erc20ProxyWrapper(
    contractAddress: String,
    credentials: Credentials,
    contractGasProvider: ContractGasProvider,
    providerUrl: String,
    private var proxyAddress: String
) : Contract(BINARY, contractAddress, Web3j.build(HttpService(providerUrl)), credentials, contractGasProvider),
    IErc20Proxy {

    override fun lockProxy(): Flowable<TransactionReceipt> = approve(proxyAddress, BigInteger.valueOf(0))

    override fun setUnlimitedProxyAllowance(): Flowable<TransactionReceipt> = approve(proxyAddress, MAX_ALLOWANCE)

    override fun proxyAllowance(ownerAddress: String): Flowable<BigInteger> = allowance(ownerAddress, proxyAddress)

    private fun allowance(ownerAddress: String, spenderAddress: String): Flowable<BigInteger> {
        val function = Function(
            FUNC_ALLOWANCE,
            listOf<Type<*>>(Address(ownerAddress), Address(spenderAddress)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {})
        )
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java).flowable()
    }

    private fun approve(spenderAddress: String, amount: BigInteger): Flowable<TransactionReceipt> {
        val function = Function(
            FUNC_APPROVE,
            listOf<Type<*>>(Address(spenderAddress), Uint256(amount)),
            listOf<TypeReference<*>>()
        )
        return executeRemoteCallTransaction(function).flowable()
    }

    companion object {
        internal const val FUNC_ALLOWANCE = "allowance"
        internal const val FUNC_APPROVE = "approve"

        private const val BINARY = ""
    }
}
